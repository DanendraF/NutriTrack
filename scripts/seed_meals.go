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
	serviceAccountPath = "./serviceAccountKey.json"
	projectID          = "nutritrack-uiifrl25"
)

type Meal struct {
	UserID      string    `firestore:"userId"`
	FoodID      string    `firestore:"foodId"`
	FoodName    string    `firestore:"foodName"`
	Date        string    `firestore:"date"`
	MealType    string    `firestore:"mealType"`
	Quantity    float64   `firestore:"quantity"`
	ServingSize string    `firestore:"servingSize"`
	Calories    int       `firestore:"calories"`
	Protein     int       `firestore:"protein"`
	Carbs       int       `firestore:"carbs"`
	Fat         int       `firestore:"fat"`
	Timestamp   time.Time `firestore:"timestamp"`
}

func main() {
	ctx := context.Background()

	// Initialize Firestore client
	client, err := firestore.NewClient(ctx, projectID, option.WithCredentialsFile(serviceAccountPath))
	if err != nil {
		log.Fatalf("Failed to create Firestore client: %v", err)
	}
	defer client.Close()

	fmt.Printf("🍽️ Seeding meal data for user: %s\n", userID)

	// Check if user already has meals
	existingMeals, err := client.Collection("meals").Where("userId", "==", userID).Limit(1).Documents(ctx).GetAll()
	if err != nil {
		log.Fatalf("Failed to check existing meals: %v", err)
	}

	if len(existingMeals) > 0 {
		fmt.Println("⏭️ User already has meals, skipping initialization")
		return
	}

	fmt.Println("✨ No meals found, initializing sample data...")

	// Seed meals for last 7 days
	now := time.Now()
	layout := "2006-01-02"

	for daysAgo := 0; daysAgo <= 6; daysAgo++ {
		date := now.AddDate(0, 0, -daysAgo)
		dateStr := date.Format(layout)

		fmt.Printf("📅 Creating meals for: %s\n", dateStr)

		var meals []Meal

		// Rotate meal patterns
		switch daysAgo % 3 {
		case 0:
			meals = getMealsDay1(dateStr, date)
		case 1:
			meals = getMealsDay2(dateStr, date)
		case 2:
			meals = getMealsDay3(dateStr, date)
		}

		// Save each meal to Firestore
		for _, meal := range meals {
			docID := fmt.Sprintf("%s_%s_%s", userID, dateStr, meal.MealType)
			_, err := client.Collection("meals").Doc(docID).Set(ctx, meal)
			if err != nil {
				log.Printf("  ❌ Failed to add meal %s: %v", meal.FoodName, err)
			} else {
				fmt.Printf("  ✅ Added: %s (%d kcal)\n", meal.FoodName, meal.Calories)
			}
		}
	}

	fmt.Println("✅ Successfully seeded meals for 7 days!")
}

func getMealsDay1(date string, timestamp time.Time) []Meal {
	return []Meal{
		{
			UserID:      userID,
			FoodID:      "food_banana",
			FoodName:    "Banana",
			Date:        date,
			MealType:    "breakfast",
			Quantity:    1.0,
			ServingSize: "1 medium",
			Calories:    89,
			Protein:     1,
			Carbs:       23,
			Fat:         0,
			Timestamp:   timestamp,
		},
		{
			UserID:      userID,
			FoodID:      "food_chicken",
			FoodName:    "Chicken Breast (Cooked)",
			Date:        date,
			MealType:    "lunch",
			Quantity:    1.5,
			ServingSize: "100g",
			Calories:    165,
			Protein:     31,
			Carbs:       0,
			Fat:         4,
			Timestamp:   timestamp,
		},
		{
			UserID:      userID,
			FoodID:      "food_rice",
			FoodName:    "White Rice",
			Date:        date,
			MealType:    "dinner",
			Quantity:    2.0,
			ServingSize: "100g",
			Calories:    130,
			Protein:     3,
			Carbs:       28,
			Fat:         0,
			Timestamp:   timestamp,
		},
	}
}

func getMealsDay2(date string, timestamp time.Time) []Meal {
	return []Meal{
		{
			UserID:      userID,
			FoodID:      "food_oatmeal",
			FoodName:    "Oatmeal",
			Date:        date,
			MealType:    "breakfast",
			Quantity:    1.0,
			ServingSize: "100g",
			Calories:    68,
			Protein:     2,
			Carbs:       12,
			Fat:         1,
			Timestamp:   timestamp,
		},
		{
			UserID:      userID,
			FoodID:      "food_salmon",
			FoodName:    "Grilled Salmon",
			Date:        date,
			MealType:    "lunch",
			Quantity:    1.0,
			ServingSize: "100g",
			Calories:    206,
			Protein:     22,
			Carbs:       0,
			Fat:         13,
			Timestamp:   timestamp,
		},
	}
}

func getMealsDay3(date string, timestamp time.Time) []Meal {
	return []Meal{
		{
			UserID:      userID,
			FoodID:      "food_egg",
			FoodName:    "Boiled Egg",
			Date:        date,
			MealType:    "breakfast",
			Quantity:    2.0,
			ServingSize: "1 egg",
			Calories:    78,
			Protein:     6,
			Carbs:       1,
			Fat:         5,
			Timestamp:   timestamp,
		},
		{
			UserID:      userID,
			FoodID:      "food_broccoli",
			FoodName:    "Steamed Broccoli",
			Date:        date,
			MealType:    "lunch",
			Quantity:    1.5,
			ServingSize: "100g",
			Calories:    35,
			Protein:     2,
			Carbs:       7,
			Fat:         0,
			Timestamp:   timestamp,
		},
		{
			UserID:      userID,
			FoodID:      "food_pasta",
			FoodName:    "Whole Wheat Pasta",
			Date:        date,
			MealType:    "dinner",
			Quantity:    1.0,
			ServingSize: "100g",
			Calories:    124,
			Protein:     5,
			Carbs:       26,
			Fat:         1,
			Timestamp:   timestamp,
		},
	}
}
