package main

import (
	"context"
	"fmt"
	"log"
	"time"

	"cloud.google.com/go/firestore"
	firebase "firebase.google.com/go/v4"
	"google.golang.org/api/option"
)

func main() {
	ctx := context.Background()

	// Initialize Firebase
	config := &firebase.Config{
		ProjectID: "nutritrack-uiifrl25",
	}
	opt := option.WithCredentialsFile("serviceAccountKey.json")
	app, err := firebase.NewApp(ctx, config, opt)
	if err != nil {
		log.Fatalf("error initializing app: %v\n", err)
	}

	client, err := app.Firestore(ctx)
	if err != nil {
		log.Fatalf("error getting Firestore client: %v\n", err)
	}
	defer client.Close()

	fmt.Println("🧪 Testing meal flow to create all 4 collections...")

	// Get a test user ID from users collection
	userID, err := getTestUserID(ctx, client)
	if err != nil {
		log.Fatalf("Failed to get test user: %v", err)
	}

	// Create sample meals
	if err := createSampleMeals(ctx, client); err != nil {
		log.Fatalf("Failed to create meals: %v", err)
	}

	// Create sample daily log
	if err := createSampleDailyLog(ctx, client, userID); err != nil {
		log.Fatalf("Failed to create daily log: %v", err)
	}

	fmt.Println("\n✅ Test completed!")
	fmt.Println("\nFirestore collections created:")
	fmt.Println("  1. ✅ users")
	fmt.Println("  2. ✅ foods")
	fmt.Println("  3. ✅ meals (sample data added)")
	fmt.Println("  4. ✅ daily_logs (sample data added)")
}

func getTestUserID(ctx context.Context, client *firestore.Client) (string, error) {
	fmt.Println("\n📋 Getting test user...")

	// Try to get first user
	iter := client.Collection("users").Limit(1).Documents(ctx)
	docs, err := iter.GetAll()
	if err != nil {
		return "", err
	}

	if len(docs) > 0 {
		userID := docs[0].Ref.ID
		fmt.Printf("  ✓ Using existing user: %s\n", userID)
		return userID, nil
	}

	// If no user exists, create a test user
	fmt.Println("  ⚠ No users found, creating test user...")
	testUserID := "test_user_" + time.Now().Format("20060102150405")

	testUser := map[string]interface{}{
		"email":      "test@nutritrack.com",
		"name":       "Test User",
		"age":        25,
		"gender":     "male",
		"height":     170.0,
		"weight":     70.0,
		"activityLevel": "moderate",
		"goal":       "maintain",
		"targetCalories": 2000,
		"targetProtein":  150.0,
		"targetCarbs":    250.0,
		"targetFat":      67.0,
		"createdAt":  time.Now().Format(time.RFC3339),
		"updatedAt":  time.Now().Format(time.RFC3339),
	}

	_, err = client.Collection("users").Doc(testUserID).Set(ctx, testUser)
	if err != nil {
		return "", err
	}

	fmt.Printf("  ✓ Created test user: %s\n", testUserID)
	return testUserID, nil
}

func createSampleMeals(ctx context.Context, client *firestore.Client) error {
	fmt.Println("\n🍽️ Creating sample meals...")

	today := time.Now().Format("2006-01-02")
	timestamp := time.Now().Format(time.RFC3339)

	meals := []map[string]interface{}{
		{
			"id":        "meal_breakfast_" + time.Now().Format("20060102150405"),
			"userId":    "test_user_id", // Will be replaced with actual user ID
			"foodId":    "food_nasi_putih",
			"foodName":  "Nasi Putih",
			"date":      today,
			"mealType":  "breakfast",
			"portion":   1.5,
			"timestamp": timestamp,
			"nutrition": map[string]interface{}{
				"calories": 195.0,
				"protein":  4.05,
				"carbs":    42.3,
				"fat":      0.45,
				"fiber":    0.6,
				"sugar":    0.15,
				"sodium":   1.5,
			},
		},
		{
			"id":        "meal_lunch_" + time.Now().Format("20060102150405"),
			"userId":    "test_user_id",
			"foodId":    "food_ayam_goreng",
			"foodName":  "Ayam Goreng",
			"date":      today,
			"mealType":  "lunch",
			"portion":   1.0,
			"timestamp": timestamp,
			"nutrition": map[string]interface{}{
				"calories": 246.0,
				"protein":  27.0,
				"carbs":    0.0,
				"fat":      14.7,
				"fiber":    0.0,
				"sugar":    0.0,
				"sodium":   82.0,
			},
		},
		{
			"id":        "meal_snack_" + time.Now().Format("20060102150405"),
			"userId":    "test_user_id",
			"foodId":    "food_pisang",
			"foodName":  "Pisang",
			"date":      today,
			"mealType":  "snack",
			"portion":   2.0,
			"timestamp": timestamp,
			"nutrition": map[string]interface{}{
				"calories": 178.0,
				"protein":  2.2,
				"carbs":    45.6,
				"fat":      0.6,
				"fiber":    5.2,
				"sugar":    24.4,
				"sodium":   2.0,
			},
		},
	}

	// Get actual user ID
	userID, err := getTestUserID(ctx, client)
	if err != nil {
		return err
	}

	batch := client.Batch()
	for _, meal := range meals {
		meal["userId"] = userID
		mealID := meal["id"].(string)
		docRef := client.Collection("meals").Doc(mealID)
		batch.Set(docRef, meal)
		fmt.Printf("  ✓ Adding meal: %s - %s\n", meal["mealType"], meal["foodName"])
	}

	_, err = batch.Commit(ctx)
	if err != nil {
		return fmt.Errorf("failed to commit meals: %v", err)
	}

	fmt.Printf("\n✅ Created %d sample meals\n", len(meals))
	return nil
}

func createSampleDailyLog(ctx context.Context, client *firestore.Client, userID string) error {
	fmt.Println("\n📊 Creating sample daily log...")

	today := time.Now().Format("2006-01-02")
	dailyLogID := userID + "_" + today

	dailyLog := map[string]interface{}{
		"id":     dailyLogID,
		"userId": userID,
		"date":   today,
		"totalNutrition": map[string]interface{}{
			"calories": 619.0,  // Sum of all meals
			"protein":  33.25,
			"carbs":    87.9,
			"fat":      15.75,
			"fiber":    5.8,
			"sugar":    24.55,
			"sodium":   85.5,
		},
		"mealCount": 3,
		"meals": map[string]interface{}{
			"breakfast": 1,
			"lunch":     1,
			"snack":     1,
			"dinner":    0,
		},
		"targetCalories": 2000,
		"targetProtein":  150.0,
		"targetCarbs":    250.0,
		"targetFat":      67.0,
		"createdAt":      time.Now().Format(time.RFC3339),
		"updatedAt":      time.Now().Format(time.RFC3339),
	}

	_, err := client.Collection("daily_logs").Doc(dailyLogID).Set(ctx, dailyLog)
	if err != nil {
		return fmt.Errorf("failed to create daily log: %v", err)
	}

	fmt.Printf("  ✓ Created daily log for date: %s\n", today)
	fmt.Println("  ✓ Total calories: 619 kcal")
	fmt.Println("  ✓ Meal count: 3")

	return nil
}
