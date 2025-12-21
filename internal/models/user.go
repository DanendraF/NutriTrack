package models

import "time"

// User represents a user in the system
type User struct {
	ID           string       `json:"id" firestore:"id"`
	Email        string       `json:"email" firestore:"email"`
	Name         string       `json:"name" firestore:"name"`
	DateOfBirth  string       `json:"dateOfBirth" firestore:"dateOfBirth"` // YYYY-MM-DD
	Gender       string       `json:"gender" firestore:"gender"`           // "male" or "female"
	Measurements Measurements `json:"measurements" firestore:"measurements"`
	Goals        Goals        `json:"goals" firestore:"goals"`
	Settings     Settings     `json:"settings" firestore:"settings"`
	CreatedAt    time.Time    `json:"createdAt" firestore:"createdAt"`
	UpdatedAt    time.Time    `json:"updatedAt" firestore:"updatedAt"`
}

// Measurements represents user physical measurements
type Measurements struct {
	Height    float64   `json:"height" firestore:"height"`       // in cm
	Weight    float64   `json:"weight" firestore:"weight"`       // in kg
	UpdatedAt time.Time `json:"updatedAt" firestore:"updatedAt"`
}

// Goals represents user nutrition goals
type Goals struct {
	ActivityLevel   string  `json:"activityLevel" firestore:"activityLevel"`     // sedentary, light, moderate, active, very_active
	NutritionGoal   string  `json:"nutritionGoal" firestore:"nutritionGoal"`     // lose, maintain, gain
	TargetCalories  int     `json:"targetCalories" firestore:"targetCalories"`   // calculated daily calories
	TargetProtein   float64 `json:"targetProtein" firestore:"targetProtein"`     // in grams
	TargetCarbs     float64 `json:"targetCarbs" firestore:"targetCarbs"`         // in grams
	TargetFat       float64 `json:"targetFat" firestore:"targetFat"`             // in grams
	BMR             float64 `json:"bmr" firestore:"bmr"`                         // Basal Metabolic Rate
	TDEE            float64 `json:"tdee" firestore:"tdee"`                       // Total Daily Energy Expenditure
	UpdatedAt       time.Time `json:"updatedAt" firestore:"updatedAt"`
}

// Settings represents user app settings
type Settings struct {
	Units         string `json:"units" firestore:"units"`                 // "metric" or "imperial"
	Notifications bool   `json:"notifications" firestore:"notifications"` // enable/disable notifications
	Theme         string `json:"theme" firestore:"theme"`                 // "light", "dark", "system"
}

// CreateUserRequest represents the request body for creating a user
type CreateUserRequest struct {
	Email       string  `json:"email" binding:"required,email"`
	Name        string  `json:"name" binding:"required"`
	DateOfBirth string  `json:"dateOfBirth" binding:"required"` // YYYY-MM-DD
	Gender      string  `json:"gender" binding:"required"`      // "male" or "female"
	Height      float64 `json:"height" binding:"required,gt=0"` // in cm
	Weight      float64 `json:"weight" binding:"required,gt=0"` // in kg
}

// UpdateUserRequest represents the request body for updating user profile
type UpdateUserRequest struct {
	Name        *string  `json:"name,omitempty"`
	DateOfBirth *string  `json:"dateOfBirth,omitempty"`
	Height      *float64 `json:"height,omitempty"`
	Weight      *float64 `json:"weight,omitempty"`
}

// UpdateGoalsRequest represents the request body for updating user goals
type UpdateGoalsRequest struct {
	ActivityLevel *string `json:"activityLevel,omitempty"` // sedentary, light, moderate, active, very_active
	NutritionGoal *string `json:"nutritionGoal,omitempty"` // lose, maintain, gain
}

// UserResponse represents the response for user endpoints
type UserResponse struct {
	ID           string       `json:"id"`
	Email        string       `json:"email"`
	Name         string       `json:"name"`
	DateOfBirth  string       `json:"dateOfBirth"`
	Gender       string       `json:"gender"`
	Measurements Measurements `json:"measurements"`
	Goals        Goals        `json:"goals"`
	Settings     Settings     `json:"settings"`
	CreatedAt    time.Time    `json:"createdAt"`
	UpdatedAt    time.Time    `json:"updatedAt"`
}

// ToResponse converts User to UserResponse
func (u *User) ToResponse() *UserResponse {
	return &UserResponse{
		ID:           u.ID,
		Email:        u.Email,
		Name:         u.Name,
		DateOfBirth:  u.DateOfBirth,
		Gender:       u.Gender,
		Measurements: u.Measurements,
		Goals:        u.Goals,
		Settings:     u.Settings,
		CreatedAt:    u.CreatedAt,
		UpdatedAt:    u.UpdatedAt,
	}
}
