package repository

import (
	"context"
	"log"

	"github.com/nutritrack/backend/internal/models"
)

// SeedFoods seeds the database with initial Indonesian foods
func (r *FoodRepository) SeedFoods(ctx context.Context) error {
	// Check if foods already exist
	count, err := r.CountFoods(ctx)
	if err != nil {
		return err
	}

	if count > 0 {
		log.Printf("✅ Food database already seeded with %d items", count)
		return nil
	}

	log.Println("🌱 Seeding food database with Indonesian foods...")

	foods := getIndonesianFoods()

	err = r.BulkCreateFoods(ctx, foods)
	if err != nil {
		return err
	}

	log.Printf("✅ Successfully seeded %d foods to database", len(foods))
	return nil
}

// getIndonesianFoods returns a list of common Indonesian foods
func getIndonesianFoods() []*models.Food {
	return []*models.Food{
		// BUAH-BUAHAN (FRUITS)
		{
			Name:           "Banana",
			NameIndonesian: "Pisang",
			Category:       "fruits",
			Nutrition: models.Nutrition{
				Calories: 89,
				Protein:  1.1,
				Carbs:    22.8,
				Fat:      0.3,
				Fiber:    2.6,
				Sugar:    12.2,
				Sodium:   1,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Apple",
			NameIndonesian: "Apel",
			Category:       "fruits",
			Nutrition: models.Nutrition{
				Calories: 52,
				Protein:  0.3,
				Carbs:    13.8,
				Fat:      0.2,
				Fiber:    2.4,
				Sugar:    10.4,
				Sodium:   1,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Mango",
			NameIndonesian: "Mangga",
			Category:       "fruits",
			Nutrition: models.Nutrition{
				Calories: 60,
				Protein:  0.8,
				Carbs:    15,
				Fat:      0.4,
				Fiber:    1.6,
				Sugar:    13.7,
				Sodium:   1,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Papaya",
			NameIndonesian: "Pepaya",
			Category:       "fruits",
			Nutrition: models.Nutrition{
				Calories: 43,
				Protein:  0.5,
				Carbs:    11,
				Fat:      0.3,
				Fiber:    1.7,
				Sugar:    7.8,
				Sodium:   8,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Watermelon",
			NameIndonesian: "Semangka",
			Category:       "fruits",
			Nutrition: models.Nutrition{
				Calories: 30,
				Protein:  0.6,
				Carbs:    7.6,
				Fat:      0.2,
				Fiber:    0.4,
				Sugar:    6.2,
				Sodium:   1,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Orange",
			NameIndonesian: "Jeruk",
			Category:       "fruits",
			Nutrition: models.Nutrition{
				Calories: 47,
				Protein:  0.9,
				Carbs:    11.8,
				Fat:      0.1,
				Fiber:    2.4,
				Sugar:    9.4,
				Sodium:   0,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},

		// SAYURAN (VEGETABLES)
		{
			Name:           "White Rice (Cooked)",
			NameIndonesian: "Nasi Putih",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 130,
				Protein:  2.7,
				Carbs:    28.2,
				Fat:      0.3,
				Fiber:    0.4,
				Sugar:    0.1,
				Sodium:   1,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Spinach",
			NameIndonesian: "Bayam",
			Category:       "vegetables",
			Nutrition: models.Nutrition{
				Calories: 23,
				Protein:  2.9,
				Carbs:    3.6,
				Fat:      0.4,
				Fiber:    2.2,
				Sugar:    0.4,
				Sodium:   79,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Carrot",
			NameIndonesian: "Wortel",
			Category:       "vegetables",
			Nutrition: models.Nutrition{
				Calories: 41,
				Protein:  0.9,
				Carbs:    9.6,
				Fat:      0.2,
				Fiber:    2.8,
				Sugar:    4.7,
				Sodium:   69,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Tomato",
			NameIndonesian: "Tomat",
			Category:       "vegetables",
			Nutrition: models.Nutrition{
				Calories: 18,
				Protein:  0.9,
				Carbs:    3.9,
				Fat:      0.2,
				Fiber:    1.2,
				Sugar:    2.6,
				Sodium:   5,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Cabbage",
			NameIndonesian: "Kubis",
			Category:       "vegetables",
			Nutrition: models.Nutrition{
				Calories: 25,
				Protein:  1.3,
				Carbs:    5.8,
				Fat:      0.1,
				Fiber:    2.5,
				Sugar:    3.2,
				Sodium:   18,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Green Beans",
			NameIndonesian: "Buncis",
			Category:       "vegetables",
			Nutrition: models.Nutrition{
				Calories: 31,
				Protein:  1.8,
				Carbs:    7,
				Fat:      0.2,
				Fiber:    2.7,
				Sugar:    3.3,
				Sodium:   6,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},

		// PROTEIN
		{
			Name:           "Chicken Breast (Cooked)",
			NameIndonesian: "Dada Ayam",
			Category:       "protein",
			Nutrition: models.Nutrition{
				Calories: 165,
				Protein:  31,
				Carbs:    0,
				Fat:      3.6,
				Fiber:    0,
				Sugar:    0,
				Sodium:   74,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Egg (Boiled)",
			NameIndonesian: "Telur Rebus",
			Category:       "protein",
			Nutrition: models.Nutrition{
				Calories: 155,
				Protein:  13,
				Carbs:    1.1,
				Fat:      11,
				Fiber:    0,
				Sugar:    1.1,
				Sodium:   124,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Tofu",
			NameIndonesian: "Tahu",
			Category:       "protein",
			Nutrition: models.Nutrition{
				Calories: 76,
				Protein:  8,
				Carbs:    1.9,
				Fat:      4.8,
				Fiber:    0.3,
				Sugar:    0.7,
				Sodium:   7,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Tempeh",
			NameIndonesian: "Tempe",
			Category:       "protein",
			Nutrition: models.Nutrition{
				Calories: 193,
				Protein:  19,
				Carbs:    9,
				Fat:      11,
				Fiber:    0,
				Sugar:    0,
				Sodium:   9,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Fish (Tilapia, Cooked)",
			NameIndonesian: "Ikan Nila",
			Category:       "protein",
			Nutrition: models.Nutrition{
				Calories: 128,
				Protein:  26,
				Carbs:    0,
				Fat:      2.7,
				Fiber:    0,
				Sugar:    0,
				Sodium:   52,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Beef (Lean, Cooked)",
			NameIndonesian: "Daging Sapi",
			Category:       "protein",
			Nutrition: models.Nutrition{
				Calories: 250,
				Protein:  26,
				Carbs:    0,
				Fat:      15,
				Fiber:    0,
				Sugar:    0,
				Sodium:   72,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},

		// DAIRY
		{
			Name:           "Milk (Low Fat)",
			NameIndonesian: "Susu Rendah Lemak",
			Category:       "dairy",
			Nutrition: models.Nutrition{
				Calories: 42,
				Protein:  3.4,
				Carbs:    5,
				Fat:      1,
				Fiber:    0,
				Sugar:    5,
				Sodium:   44,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "ml"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Yogurt (Plain)",
			NameIndonesian: "Yogurt Tawar",
			Category:       "dairy",
			Nutrition: models.Nutrition{
				Calories: 59,
				Protein:  3.5,
				Carbs:    4.7,
				Fat:      3.3,
				Fiber:    0,
				Sugar:    4.7,
				Sodium:   46,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Cheese (Cheddar)",
			NameIndonesian: "Keju Cheddar",
			Category:       "dairy",
			Nutrition: models.Nutrition{
				Calories: 403,
				Protein:  25,
				Carbs:    1.3,
				Fat:      33,
				Fiber:    0,
				Sugar:    0.5,
				Sodium:   621,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},

		// GRAINS & CARBS
		{
			Name:           "Brown Rice (Cooked)",
			NameIndonesian: "Nasi Merah",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 111,
				Protein:  2.6,
				Carbs:    23,
				Fat:      0.9,
				Fiber:    1.8,
				Sugar:    0.4,
				Sodium:   5,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Bread (Whole Wheat)",
			NameIndonesian: "Roti Gandum",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 247,
				Protein:  13,
				Carbs:    41,
				Fat:      3.4,
				Fiber:    7,
				Sugar:    6,
				Sodium:   400,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Oatmeal (Cooked)",
			NameIndonesian: "Oatmeal",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 71,
				Protein:  2.5,
				Carbs:    12,
				Fat:      1.5,
				Fiber:    1.7,
				Sugar:    0.3,
				Sodium:   49,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Potato (Boiled)",
			NameIndonesian: "Kentang Rebus",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 87,
				Protein:  1.9,
				Carbs:    20,
				Fat:      0.1,
				Fiber:    1.8,
				Sugar:    0.9,
				Sodium:   6,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Sweet Potato (Boiled)",
			NameIndonesian: "Ubi Jalar Rebus",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 90,
				Protein:  2,
				Carbs:    21,
				Fat:      0.2,
				Fiber:    3.3,
				Sugar:    6.5,
				Sodium:   27,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},

		// SNACKS & TRADITIONAL FOODS
		{
			Name:           "Peanuts (Roasted)",
			NameIndonesian: "Kacang Tanah Goreng",
			Category:       "snacks",
			Nutrition: models.Nutrition{
				Calories: 567,
				Protein:  26,
				Carbs:    16,
				Fat:      49,
				Fiber:    8.5,
				Sugar:    4,
				Sodium:   18,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Cassava (Boiled)",
			NameIndonesian: "Singkong Rebus",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 112,
				Protein:  1,
				Carbs:    27,
				Fat:      0.2,
				Fiber:    1.4,
				Sugar:    1.7,
				Sodium:   8,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
		{
			Name:           "Corn (Boiled)",
			NameIndonesian: "Jagung Rebus",
			Category:       "grains",
			Nutrition: models.Nutrition{
				Calories: 96,
				Protein:  3.4,
				Carbs:    21,
				Fat:      1.5,
				Fiber:    2.4,
				Sugar:    4.5,
				Sodium:   15,
			},
			ServingSize: models.ServingSize{Amount: 100, Unit: "g"},
			IsVerified:  true,
			Source:      "system",
		},
	}
}
