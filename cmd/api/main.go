package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"github.com/nutritrack/backend/internal/config"
	"github.com/nutritrack/backend/internal/handlers"
	"github.com/nutritrack/backend/internal/middleware"
	"github.com/nutritrack/backend/internal/repository"
	"github.com/nutritrack/backend/pkg/firebase"
)

func main() {
	// Panic recovery to catch any early crashes
	defer func() {
		if r := recover(); r != nil {
			log.Printf("❌ PANIC: %v", r)
			os.Exit(1)
		}
	}()

	// Force immediate output flush
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	log.SetOutput(os.Stdout)

	log.Println("🔧 Starting NutriTrack Backend...")
	os.Stdout.Sync() // Force flush

	// Load environment variables
	if err := godotenv.Load(); err != nil {
		log.Println("⚠️  No .env file found, using system environment variables")
	}

	// Load configuration
	cfg := config.Load()
	log.Printf("📝 Environment: %s", cfg.Environment)
	log.Printf("📝 Port: %s", cfg.Port)
	log.Printf("📝 Firebase Credentials Path: %s", cfg.FirebaseCredentialsPath)

	// Initialize Firebase
	log.Println("🔥 Initializing Firebase Admin SDK...")
	firebaseApp, err := firebase.InitializeApp(context.Background(), cfg.FirebaseCredentialsPath)
	if err != nil {
		log.Printf("❌ Failed to initialize Firebase: %v", err)
		log.Println("💡 Tip: Download serviceAccountKey.json from Firebase Console")
		log.Println("💡 See FIREBASE_SETUP.md for instructions")
		log.Fatalf("❌ Cannot start server without Firebase credentials")
	}
	defer firebaseApp.Close()
	log.Println("✅ Firebase initialized successfully")

	// Initialize repositories
	log.Println("📦 Initializing repositories...")
	userRepo, err := repository.NewUserRepository(firebaseApp)
	if err != nil {
		log.Fatalf("❌ Failed to initialize user repository: %v", err)
	}
	defer userRepo.Close()

	foodRepo, err := repository.NewFoodRepository(firebaseApp)
	if err != nil {
		log.Fatalf("❌ Failed to initialize food repository: %v", err)
	}
	defer foodRepo.Close()
	log.Println("✅ Repositories initialized")

	// Seed food database
	log.Println("🌱 Checking food database...")
	if err := foodRepo.SeedFoods(context.Background()); err != nil {
		log.Printf("⚠️  Failed to seed foods: %v", err)
	}

	// Initialize handlers
	log.Println("🔧 Initializing handlers...")
	userHandler := handlers.NewUserHandler(userRepo)
	foodHandler := handlers.NewFoodHandler(foodRepo)
	log.Println("✅ Handlers initialized")

	// Set Gin mode
	if cfg.Environment == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	// Create Gin router
	router := gin.Default()

	// Apply middleware
	router.Use(middleware.CORS())
	router.Use(middleware.Logger())
	router.Use(middleware.Recovery())

	// Health check endpoint
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status":  "healthy",
			"service": "nutritrack-backend",
			"version": "1.0.0",
			"time":    time.Now().Format(time.RFC3339),
		})
	})

	// API v1 routes
	v1 := router.Group("/api/v1")
	{
		// Public routes
		v1.GET("/ping", handlers.Ping)

		// Protected routes (require authentication)
		auth := v1.Group("")
		auth.Use(middleware.FirebaseAuth(firebaseApp))
		{
			// User routes
			users := auth.Group("/users")
			{
				users.POST("", userHandler.CreateUser)          // Create new user profile
				users.GET("/me", userHandler.GetCurrentUser)    // Get current user
				users.PUT("/me", userHandler.UpdateUser)        // Update profile
				users.PUT("/me/goals", userHandler.UpdateGoals) // Update nutrition goals
				users.DELETE("/me", userHandler.DeleteUser)     // Delete account
			}

			// Meal routes (TODO: implement)
			meals := auth.Group("/meals")
			{
				meals.GET("", handlers.GetMeals)
				meals.POST("", handlers.CreateMeal)
				meals.GET("/:id", handlers.GetMeal)
				meals.PUT("/:id", handlers.UpdateMeal)
				meals.DELETE("/:id", handlers.DeleteMeal)
			}

			// Daily log routes (TODO: implement)
			dailyLogs := auth.Group("/daily-logs")
			{
				dailyLogs.GET("", handlers.GetDailyLogs)
				dailyLogs.GET("/:date", handlers.GetDailyLog)
			}

			// Food routes
			foods := auth.Group("/foods")
			{
				foods.GET("", foodHandler.SearchFoods)                 // Search foods with query params
				foods.GET("/:id", foodHandler.GetFood)                 // Get food by ID
				foods.GET("/barcode/:barcode", foodHandler.GetFoodByBarcode) // Get by barcode
				foods.GET("/category/:category", foodHandler.GetFoodsByCategory) // Get by category
				foods.POST("", foodHandler.CreateFood)                 // Create food (admin)
				foods.PUT("/:id", foodHandler.UpdateFood)              // Update food (admin)
				foods.DELETE("/:id", foodHandler.DeleteFood)           // Delete food (admin)
			}
		}
	}

	// Create HTTP server
	srv := &http.Server{
		Addr:         ":" + cfg.Port,
		Handler:      router,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	// Start server in goroutine
	go func() {
		log.Printf("🚀 Server starting on port %s...", cfg.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start server: %v", err)
		}
	}()

	// Wait for interrupt signal to gracefully shutdown the server
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, os.Interrupt, syscall.SIGTERM)
	log.Println("⏳ Server is running. Press Ctrl+C to stop...")
	<-quit

	log.Println("🛑 Shutting down server...")

	// Graceful shutdown with 5 second timeout
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("Server forced to shutdown: %v", err)
	}

	log.Println("✅ Server exited")
}
