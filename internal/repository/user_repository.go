package repository

import (
	"context"
	"time"

	"cloud.google.com/go/firestore"
	"github.com/nutritrack/backend/internal/models"
	"github.com/nutritrack/backend/pkg/firebase"
	"google.golang.org/api/iterator"
)

type UserRepository struct {
	client *firestore.Client
}

func NewUserRepository(app *firebase.FirebaseApp) (*UserRepository, error) {
	client, err := app.App.Firestore(context.Background())
	if err != nil {
		return nil, err
	}
	return &UserRepository{client: client}, nil
}

// CreateUser creates a new user in Firestore
func (r *UserRepository) CreateUser(ctx context.Context, userID string, req *models.CreateUserRequest) (*models.User, error) {
	now := time.Now()

	user := &models.User{
		ID:          userID,
		Email:       req.Email,
		Name:        req.Name,
		DateOfBirth: req.DateOfBirth,
		Gender:      req.Gender,
		Measurements: models.Measurements{
			Height:    req.Height,
			Weight:    req.Weight,
			UpdatedAt: now,
		},
		Goals: models.Goals{
			ActivityLevel:  "moderate",  // default
			NutritionGoal:  "maintain",  // default
			TargetCalories: 2000,        // default, will be calculated
			TargetProtein:  0,
			TargetCarbs:    0,
			TargetFat:      0,
			BMR:            0,
			TDEE:           0,
			UpdatedAt:      now,
		},
		Settings: models.Settings{
			Units:         "metric",
			Notifications: true,
			Theme:         "system",
		},
		CreatedAt: now,
		UpdatedAt: now,
	}

	// Save to Firestore
	_, err := r.client.Collection("users").Doc(userID).Set(ctx, user)
	if err != nil {
		return nil, err
	}

	return user, nil
}

// GetUserByID retrieves a user by ID
func (r *UserRepository) GetUserByID(ctx context.Context, userID string) (*models.User, error) {
	doc, err := r.client.Collection("users").Doc(userID).Get(ctx)
	if err != nil {
		return nil, err
	}

	var user models.User
	if err := doc.DataTo(&user); err != nil {
		return nil, err
	}

	return &user, nil
}

// UpdateUserProfile updates user profile information
func (r *UserRepository) UpdateUserProfile(ctx context.Context, userID string, req *models.UpdateUserRequest) (*models.User, error) {
	updates := []firestore.Update{
		{Path: "updatedAt", Value: time.Now()},
	}

	if req.Name != nil {
		updates = append(updates, firestore.Update{Path: "name", Value: *req.Name})
	}
	if req.DateOfBirth != nil {
		updates = append(updates, firestore.Update{Path: "dateOfBirth", Value: *req.DateOfBirth})
	}
	if req.Height != nil {
		updates = append(updates, firestore.Update{Path: "measurements.height", Value: *req.Height})
		updates = append(updates, firestore.Update{Path: "measurements.updatedAt", Value: time.Now()})
	}
	if req.Weight != nil {
		updates = append(updates, firestore.Update{Path: "measurements.weight", Value: *req.Weight})
		updates = append(updates, firestore.Update{Path: "measurements.updatedAt", Value: time.Now()})
	}

	_, err := r.client.Collection("users").Doc(userID).Update(ctx, updates)
	if err != nil {
		return nil, err
	}

	// Retrieve updated user
	return r.GetUserByID(ctx, userID)
}

// UpdateUserGoals updates user nutrition goals
func (r *UserRepository) UpdateUserGoals(ctx context.Context, userID string, req *models.UpdateGoalsRequest) (*models.User, error) {
	// First get current user to calculate new goals
	user, err := r.GetUserByID(ctx, userID)
	if err != nil {
		return nil, err
	}

	updates := []firestore.Update{
		{Path: "goals.updatedAt", Value: time.Now()},
		{Path: "updatedAt", Value: time.Now()},
	}

	if req.ActivityLevel != nil {
		updates = append(updates, firestore.Update{Path: "goals.activityLevel", Value: *req.ActivityLevel})
		user.Goals.ActivityLevel = *req.ActivityLevel
	}
	if req.NutritionGoal != nil {
		updates = append(updates, firestore.Update{Path: "goals.nutritionGoal", Value: *req.NutritionGoal})
		user.Goals.NutritionGoal = *req.NutritionGoal
	}

	// Recalculate nutrition goals
	calculatedGoals := calculateNutritionGoals(user)
	updates = append(updates,
		firestore.Update{Path: "goals.bmr", Value: calculatedGoals.BMR},
		firestore.Update{Path: "goals.tdee", Value: calculatedGoals.TDEE},
		firestore.Update{Path: "goals.targetCalories", Value: calculatedGoals.TargetCalories},
		firestore.Update{Path: "goals.targetProtein", Value: calculatedGoals.TargetProtein},
		firestore.Update{Path: "goals.targetCarbs", Value: calculatedGoals.TargetCarbs},
		firestore.Update{Path: "goals.targetFat", Value: calculatedGoals.TargetFat},
	)

	_, err = r.client.Collection("users").Doc(userID).Update(ctx, updates)
	if err != nil {
		return nil, err
	}

	return r.GetUserByID(ctx, userID)
}

// DeleteUser deletes a user
func (r *UserRepository) DeleteUser(ctx context.Context, userID string) error {
	_, err := r.client.Collection("users").Doc(userID).Delete(ctx)
	return err
}

// UserExists checks if a user exists
func (r *UserRepository) UserExists(ctx context.Context, userID string) (bool, error) {
	doc, err := r.client.Collection("users").Doc(userID).Get(ctx)
	if err != nil {
		return false, nil
	}
	return doc.Exists(), nil
}

// ListUsers lists all users (admin function)
func (r *UserRepository) ListUsers(ctx context.Context, limit int) ([]*models.User, error) {
	iter := r.client.Collection("users").Limit(limit).Documents(ctx)
	defer iter.Stop()

	var users []*models.User
	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, err
		}

		var user models.User
		if err := doc.DataTo(&user); err != nil {
			continue
		}
		users = append(users, &user)
	}

	return users, nil
}

// calculateNutritionGoals calculates BMR, TDEE, and macros based on user data
func calculateNutritionGoals(user *models.User) *models.Goals {
	// Calculate age from date of birth
	age := calculateAge(user.DateOfBirth)

	// Calculate BMR using Harris-Benedict Equation
	var bmr float64
	if user.Gender == "male" {
		bmr = 88.362 + (13.397 * user.Measurements.Weight) + (4.799 * user.Measurements.Height) - (5.677 * float64(age))
	} else {
		bmr = 447.593 + (9.247 * user.Measurements.Weight) + (3.098 * user.Measurements.Height) - (4.330 * float64(age))
	}

	// Activity multipliers
	activityMultipliers := map[string]float64{
		"sedentary":    1.2,
		"light":        1.375,
		"moderate":     1.55,
		"active":       1.725,
		"very_active":  1.9,
	}

	multiplier, ok := activityMultipliers[user.Goals.ActivityLevel]
	if !ok {
		multiplier = 1.55 // default to moderate
	}

	// Calculate TDEE
	tdee := bmr * multiplier

	// Adjust for nutrition goal
	var targetCalories int
	switch user.Goals.NutritionGoal {
	case "lose":
		targetCalories = int(tdee * 0.85) // 15% deficit
	case "gain":
		targetCalories = int(tdee * 1.15) // 15% surplus
	default: // maintain
		targetCalories = int(tdee)
	}

	// Calculate macros (40% carbs, 30% protein, 30% fat)
	targetProtein := float64(targetCalories) * 0.30 / 4    // 4 cal/g
	targetCarbs := float64(targetCalories) * 0.40 / 4      // 4 cal/g
	targetFat := float64(targetCalories) * 0.30 / 9        // 9 cal/g

	return &models.Goals{
		BMR:            bmr,
		TDEE:           tdee,
		TargetCalories: targetCalories,
		TargetProtein:  targetProtein,
		TargetCarbs:    targetCarbs,
		TargetFat:      targetFat,
		UpdatedAt:      time.Now(),
	}
}

// calculateAge calculates age from date of birth (YYYY-MM-DD)
func calculateAge(dob string) int {
	// Simple calculation - parse YYYY-MM-DD
	var year int
	_, err := time.Parse("2006-01-02", dob)
	if err != nil {
		return 25 // default age
	}

	dobTime, _ := time.Parse("2006-01-02", dob)
	year = time.Now().Year() - dobTime.Year()

	// Adjust if birthday hasn't occurred this year
	if time.Now().YearDay() < dobTime.YearDay() {
		year--
	}

	return year
}

// Close closes the Firestore client
func (r *UserRepository) Close() error {
	return r.client.Close()
}
