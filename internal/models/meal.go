package models

import "time"

// Meal represents a single meal entry
type Meal struct {
	ID        string    `json:"id" firestore:"id"`
	UserID    string    `json:"userId" firestore:"userId"`
	FoodID    string    `json:"foodId" firestore:"foodId"`
	FoodName  string    `json:"foodName" firestore:"foodName"`
	Date      string    `json:"date" firestore:"date"` // YYYY-MM-DD format
	MealType  string    `json:"mealType" firestore:"mealType"` // breakfast, lunch, dinner, snack
	Portion   float64   `json:"portion" firestore:"portion"` // Multiplier (1.0 = 1 serving)
	Nutrition Nutrition `json:"nutrition" firestore:"nutrition"` // Calculated nutrition based on portion
	Timestamp time.Time `json:"timestamp" firestore:"timestamp"`
	CreatedAt time.Time `json:"createdAt" firestore:"createdAt"`
}

// DailyLog represents a summary of all meals for a specific date
type DailyLog struct {
	ID             string    `json:"id" firestore:"id"` // userId_date
	UserID         string    `json:"userId" firestore:"userId"`
	Date           string    `json:"date" firestore:"date"` // YYYY-MM-DD
	Meals          []Meal    `json:"meals" firestore:"meals"`
	TotalCalories  float64   `json:"totalCalories" firestore:"totalCalories"`
	TotalProtein   float64   `json:"totalProtein" firestore:"totalProtein"`
	TotalCarbs     float64   `json:"totalCarbs" firestore:"totalCarbs"`
	TotalFat       float64   `json:"totalFat" firestore:"totalFat"`
	UpdatedAt      time.Time `json:"updatedAt" firestore:"updatedAt"`
}

// CreateMealRequest is the request body for creating a meal
type CreateMealRequest struct {
	FoodID    string  `json:"foodId" binding:"required"`
	FoodName  string  `json:"foodName" binding:"required"`
	Date      string  `json:"date" binding:"required"` // YYYY-MM-DD
	MealType  string  `json:"mealType" binding:"required"`
	Portion   float64 `json:"portion" binding:"required"`
	Nutrition Nutrition `json:"nutrition" binding:"required"`
}

// UpdateMealRequest is the request body for updating a meal
type UpdateMealRequest struct {
	MealType  string    `json:"mealType,omitempty"`
	Portion   float64   `json:"portion,omitempty"`
	Nutrition Nutrition `json:"nutrition,omitempty"`
}
