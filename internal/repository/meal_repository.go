package repository

import (
	"context"
	"fmt"
	"log"
	"time"

	"github.com/nutritrack/backend/internal/models"
	"github.com/nutritrack/backend/pkg/firebase"

	"cloud.google.com/go/firestore"
	"github.com/google/uuid"
	"google.golang.org/api/iterator"
)

type MealRepository struct {
	client *firestore.Client
}

func NewMealRepository(app *firebase.FirebaseApp) (*MealRepository, error) {
	client, err := app.App.Firestore(context.Background())
	if err != nil {
		return nil, fmt.Errorf("failed to get Firestore client: %w", err)
	}
	return &MealRepository{
		client: client,
	}, nil
}

// CreateMeal creates a new meal entry and updates daily log
func (r *MealRepository) CreateMeal(ctx context.Context, userID string, req *models.CreateMealRequest) (*models.Meal, error) {
	// Generate meal ID
	mealID := uuid.New().String()

	// Create meal
	meal := &models.Meal{
		ID:        mealID,
		UserID:    userID,
		FoodID:    req.FoodID,
		FoodName:  req.FoodName,
		Date:      req.Date,
		MealType:  req.MealType,
		Portion:   req.Portion,
		Nutrition: req.Nutrition,
		Timestamp: time.Now(),
		CreatedAt: time.Now(),
	}

	// Save meal to Firestore (meals collection)
	_, err := r.client.Collection("meals").Doc(mealID).Set(ctx, meal)
	if (err != nil) {
		return nil, fmt.Errorf("failed to create meal: %w", err)
	}

	// Update daily log
	err = r.updateDailyLog(ctx, userID, req.Date)
	if err != nil {
		log.Printf("Warning: failed to update daily log: %v", err)
		// Don't fail the meal creation if daily log update fails
	}

	return meal, nil
}

// GetMealsByUserAndDate gets all meals for a user on a specific date
func (r *MealRepository) GetMealsByUserAndDate(ctx context.Context, userID string, date string) ([]models.Meal, error) {
	log.Printf("[GetMealsByUserAndDate] Querying meals for userID=%s, date=%s", userID, date)
	var meals []models.Meal

	// Try query with OrderBy first
	iter := r.client.Collection("meals").
		Where("userId", "==", userID).
		Where("date", "==", date).
		OrderBy("timestamp", firestore.Asc).
		Documents(ctx)

	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			// If OrderBy fails (no index), try without OrderBy
			log.Printf("[GetMealsByUserAndDate] Error with OrderBy query (likely missing index), retrying without OrderBy: %v", err)

			iter2 := r.client.Collection("meals").
				Where("userId", "==", userID).
				Where("date", "==", date).
				Documents(ctx)

			for {
				doc2, err2 := iter2.Next()
				if err2 == iterator.Done {
					break
				}
				if err2 != nil {
					log.Printf("[GetMealsByUserAndDate] Error in fallback query: %v", err2)
					return nil, fmt.Errorf("failed to query meals: %w", err2)
				}

				var meal models.Meal
				if err := doc2.DataTo(&meal); err != nil {
					log.Printf("[GetMealsByUserAndDate] Error mapping meal: %v", err)
					continue
				}
				meals = append(meals, meal)
			}

			log.Printf("[GetMealsByUserAndDate] Found %d meals (fallback query)", len(meals))
			return meals, nil
		}

		var meal models.Meal
		if err := doc.DataTo(&meal); err != nil {
			log.Printf("[GetMealsByUserAndDate] Error mapping meal: %v", err)
			continue
		}
		meals = append(meals, meal)
	}

	log.Printf("[GetMealsByUserAndDate] Found %d meals", len(meals))
	return meals, nil
}

// GetMeal gets a single meal by ID
func (r *MealRepository) GetMeal(ctx context.Context, userID string, mealID string) (*models.Meal, error) {
	doc, err := r.client.Collection("meals").Doc(mealID).Get(ctx)
	if err != nil {
		return nil, fmt.Errorf("meal not found: %w", err)
	}

	var meal models.Meal
	if err := doc.DataTo(&meal); err != nil {
		return nil, fmt.Errorf("failed to parse meal: %w", err)
	}

	// Verify ownership
	if meal.UserID != userID {
		return nil, fmt.Errorf("unauthorized: meal does not belong to user")
	}

	return &meal, nil
}

// UpdateMeal updates an existing meal
func (r *MealRepository) UpdateMeal(ctx context.Context, userID string, mealID string, req *models.UpdateMealRequest) (*models.Meal, error) {
	// Get existing meal
	meal, err := r.GetMeal(ctx, userID, mealID)
	if err != nil {
		return nil, err
	}

	// Update fields if provided
	updates := make(map[string]interface{})
	if req.MealType != "" {
		meal.MealType = req.MealType
		updates["mealType"] = req.MealType
	}
	if req.Portion > 0 {
		meal.Portion = req.Portion
		updates["portion"] = req.Portion
	}
	if req.Nutrition.Calories > 0 {
		meal.Nutrition = req.Nutrition
		updates["nutrition"] = req.Nutrition
	}

	// Save updates
	_, err = r.client.Collection("meals").Doc(mealID).Set(ctx, updates, firestore.MergeAll)
	if err != nil {
		return nil, fmt.Errorf("failed to update meal: %w", err)
	}

	// Update daily log
	err = r.updateDailyLog(ctx, userID, meal.Date)
	if err != nil {
		log.Printf("Warning: failed to update daily log: %v", err)
	}

	return meal, nil
}

// DeleteMeal deletes a meal
func (r *MealRepository) DeleteMeal(ctx context.Context, userID string, mealID string) error {
	// Get meal to verify ownership and get date
	meal, err := r.GetMeal(ctx, userID, mealID)
	if err != nil {
		return err
	}

	// Delete meal
	_, err = r.client.Collection("meals").Doc(mealID).Delete(ctx)
	if err != nil {
		return fmt.Errorf("failed to delete meal: %w", err)
	}

	// Update daily log
	err = r.updateDailyLog(ctx, userID, meal.Date)
	if err != nil {
		log.Printf("Warning: failed to update daily log: %v", err)
	}

	return nil
}

// GetDailyLog gets the daily log summary for a specific date
func (r *MealRepository) GetDailyLog(ctx context.Context, userID string, date string) (*models.DailyLog, error) {
	dailyLogID := fmt.Sprintf("%s_%s", userID, date)
	log.Printf("[GetDailyLog] Fetching daily log: %s (userID=%s, date=%s)", dailyLogID, userID, date)

	doc, err := r.client.Collection("daily_logs").Doc(dailyLogID).Get(ctx)
	if err != nil {
		// If daily log doesn't exist, recalculate it
		log.Printf("[GetDailyLog] Daily log not found, recalculating... (error: %v)", err)
		return r.recalculateDailyLog(ctx, userID, date)
	}

	var dailyLog models.DailyLog
	if err := doc.DataTo(&dailyLog); err != nil {
		log.Printf("[GetDailyLog] Failed to parse daily log: %v", err)
		return nil, fmt.Errorf("failed to parse daily log: %w", err)
	}

	log.Printf("[GetDailyLog] Successfully fetched daily log with %d meals", len(dailyLog.Meals))
	return &dailyLog, nil
}

// GetDailyLogs gets daily logs for a date range
func (r *MealRepository) GetDailyLogs(ctx context.Context, userID string, startDate string, endDate string) ([]models.DailyLog, error) {
	log.Printf("[GetDailyLogs] Querying daily logs for userID=%s, startDate=%s, endDate=%s", userID, startDate, endDate)
	var dailyLogs []models.DailyLog

	// Try query with OrderBy first
	query := r.client.Collection("daily_logs").
		Where("userId", "==", userID).
		OrderBy("date", firestore.Desc)

	if startDate != "" {
		query = query.Where("date", ">=", startDate)
	}
	if endDate != "" {
		query = query.Where("date", "<=", endDate)
	}

	iter := query.Documents(ctx)
	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			// If OrderBy fails (likely missing index), try fallback without OrderBy
			log.Printf("[GetDailyLogs] Error with OrderBy query (likely missing index), using fallback: %v", err)
			return r.getDailyLogsFallback(ctx, userID, startDate, endDate)
		}

		var dailyLog models.DailyLog
		if err := doc.DataTo(&dailyLog); err != nil {
			log.Printf("[GetDailyLogs] Error mapping daily log: %v", err)
			continue
		}
		dailyLogs = append(dailyLogs, dailyLog)
	}

	log.Printf("[GetDailyLogs] Found %d daily logs", len(dailyLogs))
	return dailyLogs, nil
}

// getDailyLogsFallback gets daily logs without OrderBy (fallback when index is missing)
func (r *MealRepository) getDailyLogsFallback(ctx context.Context, userID string, startDate string, endDate string) ([]models.DailyLog, error) {
	log.Printf("[getDailyLogsFallback] Using fallback query without OrderBy")
	var dailyLogs []models.DailyLog

	// Simple query without OrderBy
	query := r.client.Collection("daily_logs").
		Where("userId", "==", userID)

	if startDate != "" {
		query = query.Where("date", ">=", startDate)
	}
	if endDate != "" {
		query = query.Where("date", "<=", endDate)
	}

	iter := query.Documents(ctx)
	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Printf("[getDailyLogsFallback] Error in fallback query: %v", err)
			return nil, fmt.Errorf("failed to iterate daily logs: %w", err)
		}

		var dailyLog models.DailyLog
		if err := doc.DataTo(&dailyLog); err != nil {
			log.Printf("[getDailyLogsFallback] Error mapping daily log: %v", err)
			continue
		}
		dailyLogs = append(dailyLogs, dailyLog)
	}

	log.Printf("[getDailyLogsFallback] Found %d daily logs (fallback)", len(dailyLogs))
	return dailyLogs, nil
}

// updateDailyLog recalculates and updates the daily log for a specific date
func (r *MealRepository) updateDailyLog(ctx context.Context, userID string, date string) error {
	dailyLog, err := r.recalculateDailyLog(ctx, userID, date)
	if err != nil {
		return err
	}

	dailyLogID := fmt.Sprintf("%s_%s", userID, date)
	_, err = r.client.Collection("daily_logs").Doc(dailyLogID).Set(ctx, dailyLog)
	if err != nil {
		return fmt.Errorf("failed to save daily log: %w", err)
	}

	return nil
}

// recalculateDailyLog calculates daily totals from all meals
func (r *MealRepository) recalculateDailyLog(ctx context.Context, userID string, date string) (*models.DailyLog, error) {
	log.Printf("[recalculateDailyLog] Getting meals for user=%s, date=%s", userID, date)

	meals, err := r.GetMealsByUserAndDate(ctx, userID, date)
	if err != nil {
		log.Printf("[recalculateDailyLog] Error getting meals: %v", err)
		return nil, err
	}

	log.Printf("[recalculateDailyLog] Found %d meals for date %s", len(meals), date)

	dailyLog := &models.DailyLog{
		ID:     fmt.Sprintf("%s_%s", userID, date),
		UserID: userID,
		Date:   date,
		Meals:  meals,
	}

	// Calculate totals
	for _, meal := range meals {
		dailyLog.TotalCalories += float64(meal.Nutrition.Calories)
		dailyLog.TotalProtein += meal.Nutrition.Protein
		dailyLog.TotalCarbs += meal.Nutrition.Carbs
		dailyLog.TotalFat += meal.Nutrition.Fat
	}

	dailyLog.UpdatedAt = time.Now()

	log.Printf("[recalculateDailyLog] Calculated totals - Calories: %.2f, Protein: %.2f, Carbs: %.2f, Fat: %.2f",
		dailyLog.TotalCalories, dailyLog.TotalProtein, dailyLog.TotalCarbs, dailyLog.TotalFat)

	return dailyLog, nil
}
