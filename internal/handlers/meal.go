package handlers

import (
	"log"
	"net/http"

	"github.com/nutritrack/backend/internal/models"
	"github.com/nutritrack/backend/internal/repository"

	"github.com/gin-gonic/gin"
)

type MealHandler struct {
	mealRepo *repository.MealRepository
}

func NewMealHandler(mealRepo *repository.MealRepository) *MealHandler {
	return &MealHandler{
		mealRepo: mealRepo,
	}
}

// CreateMeal creates a new meal entry
func (h *MealHandler) CreateMeal(c *gin.Context) {
	userID, exists := c.Get("userID")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var req models.CreateMealRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	log.Printf("📝 Creating meal for user %s: %s (%.1f portions)", userID, req.FoodName, req.Portion)

	meal, err := h.mealRepo.CreateMeal(c.Request.Context(), userID.(string), &req)
	if err != nil {
		log.Printf("❌ Failed to create meal: %v", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create meal"})
		return
	}

	log.Printf("✅ Meal created: %s", meal.ID)
	c.JSON(http.StatusCreated, meal)
}

// GetMeal gets a specific meal by ID
func (h *MealHandler) GetMeal(c *gin.Context) {
	userID, _ := c.Get("userID")
	mealID := c.Param("id")

	meal, err := h.mealRepo.GetMeal(c.Request.Context(), userID.(string), mealID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Meal not found"})
		return
	}

	c.JSON(http.StatusOK, meal)
}

// UpdateMeal updates an existing meal
func (h *MealHandler) UpdateMeal(c *gin.Context) {
	userID, _ := c.Get("userID")
	mealID := c.Param("id")

	var req models.UpdateMealRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	meal, err := h.mealRepo.UpdateMeal(c.Request.Context(), userID.(string), mealID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to update meal"})
		return
	}

	c.JSON(http.StatusOK, meal)
}

// DeleteMeal deletes a meal
func (h *MealHandler) DeleteMeal(c *gin.Context) {
	userID, _ := c.Get("userID")
	mealID := c.Param("id")

	err := h.mealRepo.DeleteMeal(c.Request.Context(), userID.(string), mealID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to delete meal"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Meal deleted successfully"})
}
