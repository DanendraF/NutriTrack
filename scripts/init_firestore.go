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

	fmt.Println("🚀 Starting Firestore initialization...")

	// 1. Create sample foods
	if err := createSampleFoods(ctx, client); err != nil {
		log.Fatalf("Failed to create foods: %v", err)
	}

	// 2. Create indexes (manual step - will be created automatically on first query)
	fmt.Println("\n📋 Note: Composite indexes will be created automatically when needed.")
	fmt.Println("If you see index errors, Firestore will provide a link to create them.")

	fmt.Println("\n✅ Firestore initialization completed!")
	fmt.Println("\nCollections created:")
	fmt.Println("  - foods (with sample data)")
	fmt.Println("\nCollections that will be auto-created:")
	fmt.Println("  - users (when user registers)")
	fmt.Println("  - meals (when user logs meal)")
	fmt.Println("  - daily_logs (auto-calculated by backend)")
	fmt.Println("  - user_saved_foods (when user saves food)")
}

func createSampleFoods(ctx context.Context, client *firestore.Client) error {
	fmt.Println("\n📦 Creating sample foods...")

	foods := []map[string]interface{}{
		{
			"id":             "food_nasi_putih",
			"name":           "White Rice",
			"nameIndonesian": "Nasi Putih",
			"category":       "grains",
			"nutrition": map[string]interface{}{
				"calories": 130,
				"protein":  2.7,
				"carbs":    28.2,
				"fat":      0.3,
				"fiber":    0.4,
				"sugar":    0.1,
				"sodium":   1.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_ayam_goreng",
			"name":           "Fried Chicken",
			"nameIndonesian": "Ayam Goreng",
			"category":       "protein",
			"nutrition": map[string]interface{}{
				"calories": 246,
				"protein":  27.0,
				"carbs":    0.0,
				"fat":      14.7,
				"fiber":    0.0,
				"sugar":    0.0,
				"sodium":   82.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_tempe_goreng",
			"name":           "Fried Tempeh",
			"nameIndonesian": "Tempe Goreng",
			"category":       "protein",
			"nutrition": map[string]interface{}{
				"calories": 193,
				"protein":  18.5,
				"carbs":    9.4,
				"fat":      11.0,
				"fiber":    0.0,
				"sugar":    0.0,
				"sodium":   9.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_telur_rebus",
			"name":           "Boiled Egg",
			"nameIndonesian": "Telur Rebus",
			"category":       "protein",
			"nutrition": map[string]interface{}{
				"calories": 155,
				"protein":  12.6,
				"carbs":    1.1,
				"fat":      10.6,
				"fiber":    0.0,
				"sugar":    1.1,
				"sodium":   124.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_sayur_bayam",
			"name":           "Spinach",
			"nameIndonesian": "Bayam",
			"category":       "vegetables",
			"nutrition": map[string]interface{}{
				"calories": 23,
				"protein":  2.9,
				"carbs":    3.6,
				"fat":      0.4,
				"fiber":    2.2,
				"sugar":    0.4,
				"sodium":   79.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_pisang",
			"name":           "Banana",
			"nameIndonesian": "Pisang",
			"category":       "fruits",
			"nutrition": map[string]interface{}{
				"calories": 89,
				"protein":  1.1,
				"carbs":    22.8,
				"fat":      0.3,
				"fiber":    2.6,
				"sugar":    12.2,
				"sodium":   1.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_roti_tawar",
			"name":           "White Bread",
			"nameIndonesian": "Roti Tawar",
			"category":       "grains",
			"nutrition": map[string]interface{}{
				"calories": 265,
				"protein":  9.0,
				"carbs":    49.0,
				"fat":      3.2,
				"fiber":    2.7,
				"sugar":    5.0,
				"sodium":   491.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_susu_sapi",
			"name":           "Cow Milk",
			"nameIndonesian": "Susu Sapi",
			"category":       "dairy",
			"nutrition": map[string]interface{}{
				"calories": 61,
				"protein":  3.2,
				"carbs":    4.8,
				"fat":      3.3,
				"fiber":    0.0,
				"sugar":    5.1,
				"sodium":   44.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "ml",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_apel",
			"name":           "Apple",
			"nameIndonesian": "Apel",
			"category":       "fruits",
			"nutrition": map[string]interface{}{
				"calories": 52,
				"protein":  0.3,
				"carbs":    13.8,
				"fat":      0.2,
				"fiber":    2.4,
				"sugar":    10.4,
				"sodium":   1.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
		{
			"id":             "food_kentang_goreng",
			"name":           "French Fries",
			"nameIndonesian": "Kentang Goreng",
			"category":       "snacks",
			"nutrition": map[string]interface{}{
				"calories": 312,
				"protein":  3.4,
				"carbs":    41.4,
				"fat":      15.0,
				"fiber":    3.8,
				"sugar":    0.3,
				"sodium":   210.0,
			},
			"servingSize": map[string]interface{}{
				"amount": 100.0,
				"unit":   "g",
			},
			"barcode":    nil,
			"imageUrl":   nil,
			"isVerified": true,
			"source":     "database",
			"createdAt":  time.Now().Format(time.RFC3339),
		},
	}

	batch := client.Batch()
	for _, food := range foods {
		docRef := client.Collection("foods").Doc(food["id"].(string))
		batch.Set(docRef, food)
		fmt.Printf("  ✓ Adding food: %s\n", food["nameIndonesian"])
	}

	_, err := batch.Commit(ctx)
	if err != nil {
		return fmt.Errorf("failed to commit batch: %v", err)
	}

	fmt.Printf("\n✅ Created %d sample foods\n", len(foods))
	return nil
}
