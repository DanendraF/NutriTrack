package main

import (
	"context"
	"fmt"
	"log"
	"time"

	"cloud.google.com/go/firestore"
	"google.golang.org/api/option"
)

const (
	userID             = "IBF45l65PTXktmB7a2u58DPu1673"
	serviceAccountPath = "serviceAccountKey.json"
	projectID          = "nutritrack-uiifrl25"
)

type User struct {
	ID             string    `firestore:"id"`
	Email          string    `firestore:"email"`
	Name           string    `firestore:"name"`
	Age            int       `firestore:"age"`
	Gender         string    `firestore:"gender"`
	Height         float64   `firestore:"height"`
	Weight         float64   `firestore:"weight"`
	ActivityLevel  string    `firestore:"activityLevel"`
	Goal           string    `firestore:"goal"`
	TargetCalories float64   `firestore:"targetCalories"`
	TargetProtein  float64   `firestore:"targetProtein"`
	TargetCarbs    float64   `firestore:"targetCarbs"`
	TargetFat      float64   `firestore:"targetFat"`
	CreatedAt      time.Time `firestore:"createdAt"`
	UpdatedAt      time.Time `firestore:"updatedAt"`
}

func main() {
	ctx := context.Background()

	// Initialize Firestore client
	client, err := firestore.NewClient(ctx, projectID, option.WithCredentialsFile(serviceAccountPath))
	if err != nil {
		log.Fatalf("Failed to create Firestore client: %v", err)
	}
	defer client.Close()

	fmt.Printf("👤 Seeding user data for user: %s\n", userID)

	// Check if user already exists
	doc, err := client.Collection("users").Doc(userID).Get(ctx)
	if err != nil && err.Error() != "rpc error: code = NotFound desc = Document not found" {
		log.Fatalf("Failed to check user existence: %v", err)
	}

	if doc.Exists() {
		fmt.Println("✅ User already exists, skipping creation")
		return
	}

	// Create user
	user := User{
		ID:             userID,
		Email:          "test@example.com",
		Name:           "Test User",
		Age:            25,
		Gender:         "male",
		Height:         170.0,
		Weight:         70.0,
		ActivityLevel:  "moderate",
		Goal:           "maintain",
		TargetCalories: 2000,
		TargetProtein:  150,
		TargetCarbs:    250,
		TargetFat:      67,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
	}

	_, err = client.Collection("users").Doc(userID).Set(ctx, user)
	if err != nil {
		log.Fatalf("Failed to create user: %v", err)
	}

	fmt.Println("✅ User created successfully!")
}