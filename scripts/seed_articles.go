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
	serviceAccountPath = "serviceAccountKey.json"
	projectID          = "nutritrack-uiifrl25"
)

type Article struct {
	ID          string    `firestore:"id"`
	Title       string    `firestore:"title"`
	Category    string    `firestore:"category"`
	ReadTime    string    `firestore:"readTime"`
	ImageUrl    string    `firestore:"imageUrl"`
	Content     string    `firestore:"content"`
	Author      string    `firestore:"author"`
	PublishedAt time.Time `firestore:"publishedAt"`
	Order       int       `firestore:"order"`
}

func main() {
	ctx := context.Background()

	// Initialize Firestore client
	client, err := firestore.NewClient(ctx, projectID, option.WithCredentialsFile(serviceAccountPath))
	if err != nil {
		log.Fatalf("Failed to create Firestore client: %v", err)
	}
	defer client.Close()

	fmt.Println("📰 Seeding article data...")

	articles := getArticles()

	// Check if articles already exist
	existingArticles, err := client.Collection("articles").Limit(1).Documents(ctx).GetAll()
	if err != nil {
		log.Fatalf("Failed to check existing articles: %v", err)
	}

	if len(existingArticles) > 0 {
		fmt.Println("⏭️ Articles already exist, skipping initialization")
		fmt.Println("💡 To re-seed, delete the 'articles' collection first")
		return
	}

	fmt.Printf("✨ Adding %d articles...\n\n", len(articles))

	// Save each article to Firestore
	for _, article := range articles {
		_, err := client.Collection("articles").Doc(article.ID).Set(ctx, article)
		if err != nil {
			log.Printf("  ❌ Failed to add article '%s': %v", article.Title, err)
		} else {
			fmt.Printf("  ✅ Added: %s (Category: %s, %s)\n", article.Title, article.Category, article.ReadTime)
		}
	}

	fmt.Println("\n✅ Successfully seeded articles!")
}

func getArticles() []Article {
	now := time.Now()

	return []Article{
		{
			ID:          "article_001",
			Title:       "Understanding Macronutrients: The Building Blocks of Nutrition",
			Category:    "Nutrition Basics",
			ReadTime:    "5 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800",
			Content:     "Macronutrients are the nutrients your body needs in large amounts to function properly. They include proteins, carbohydrates, and fats. **Proteins** are essential for building and repairing tissues, making enzymes and hormones. Good sources include lean meats, fish, eggs, legumes, and dairy. **Carbohydrates** are your body's main energy source. Choose complex carbs like whole grains, vegetables, and fruits over simple sugars. **Fats** are crucial for hormone production, nutrient absorption, and brain health. Focus on healthy fats from nuts, avocados, olive oil, and fatty fish. The key to a balanced diet is getting the right proportion of each macronutrient based on your individual needs, activity level, and health goals. A general guideline is 45-65% carbs, 10-35% protein, and 20-35% fat, but these can be adjusted based on your specific requirements.",
			Author:      "Dr. Sarah Johnson",
			PublishedAt: now.AddDate(0, 0, -30),
			Order:       1,
		},
		{
			ID:          "article_002",
			Title:       "How to Calculate Your Daily Calorie Needs",
			Category:    "Weight Management",
			ReadTime:    "4 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1511688878353-3a2f5be94cd7?w=800",
			Content:     "Calculating your daily calorie needs is crucial for achieving your health goals. Start with your **Basal Metabolic Rate (BMR)** - the calories you burn at rest. Use the Mifflin-St Jeor equation: For men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) + 5. For women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) - 161. Next, multiply your BMR by your activity factor: Sedentary (1.2), Lightly active (1.375), Moderately active (1.55), Very active (1.725), or Extra active (1.9). This gives you your Total Daily Energy Expenditure (TDEE). To lose weight, create a deficit of 500 calories per day for a healthy 1 lb per week loss. To gain weight, add 300-500 calories. Remember, these are estimates - monitor your progress and adjust as needed.",
			Author:      "Michael Chen, RD",
			PublishedAt: now.AddDate(0, 0, -28),
			Order:       2,
		},
		{
			ID:          "article_003",
			Title:       "The Importance of Protein for Muscle Growth and Recovery",
			Category:    "Fitness",
			ReadTime:    "6 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1532384748853-8f54a8f476e2?w=800",
			Content:     "Protein is essential for building and maintaining muscle mass, especially if you're active or strength training. When you exercise, you create micro-tears in your muscle fibers. Protein provides the amino acids needed to repair and strengthen these fibers, leading to muscle growth. **How much do you need?** The general recommendation is 0.8-1g per kg of body weight for sedentary individuals, but active people need more. Athletes and those building muscle should aim for 1.6-2.2g per kg. **Timing matters:** Consuming protein within 2 hours after exercise maximizes muscle protein synthesis. Distribute protein intake throughout the day (20-30g per meal) for optimal results. **Quality sources:** Choose complete proteins like eggs, chicken, fish, dairy, and plant combinations like rice and beans. Whey protein is quickly absorbed, making it ideal post-workout, while casein digests slowly, perfect before bed for overnight recovery.",
			Author:      "Coach Amanda Torres",
			PublishedAt: now.AddDate(0, 0, -25),
			Order:       3,
		},
		{
			ID:          "article_004",
			Title:       "Meal Prep 101: Save Time and Stay on Track",
			Category:    "Meal Planning",
			ReadTime:    "7 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=800",
			Content:     "Meal prepping is a game-changer for maintaining a healthy diet while managing a busy schedule. **Start simple:** Choose one meal to prep (usually lunch or dinner) and prepare 3-4 servings. **Pick your day:** Sunday or Wednesday work well for most people. **Essential equipment:** Get quality containers (glass or BPA-free plastic), a food scale, and storage bags. **The process:** 1) Plan your meals and create a shopping list. 2) Shop once for the week. 3) Batch cook proteins (chicken, fish, tofu). 4) Prep vegetables (wash, chop, roast). 5) Cook complex carbs (rice, quinoa, sweet potatoes). 6) Portion into containers. **Pro tips:** Use the freezer for meals beyond 4 days. Keep sauces separate to prevent sogginess. Prep breakfast overnight oats or egg muffins. Include variety to prevent boredom. Start with 2-3 recipes you know well before experimenting.",
			Author:      "Chef Marcus Williams",
			PublishedAt: now.AddDate(0, 0, -22),
			Order:       4,
		},
		{
			ID:          "article_005",
			Title:       "Hydration: Why Water is Essential for Your Health",
			Category:    "Wellness",
			ReadTime:    "4 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=800",
			Content:     "Water makes up about 60% of your body weight and is vital for every bodily function. **Benefits of proper hydration:** Regulates body temperature, transports nutrients, removes waste, lubricates joints, aids digestion, and improves cognitive function. **How much water do you need?** The general guideline is 8 glasses (2 liters) daily, but individual needs vary. A better approach: drink 30-35ml per kg of body weight. Increase intake when exercising, in hot weather, or if you're ill. **Signs of dehydration:** Dark urine, dry mouth, headache, fatigue, dizziness. **Hydration tips:** Start your day with a glass of water. Keep a reusable bottle with you. Set reminders on your phone. Eat water-rich foods like cucumber, watermelon, and lettuce. Flavor water with lemon, cucumber, or berries if plain water is boring. Remember: by the time you feel thirsty, you're already mildly dehydrated.",
			Author:      "Dr. Lisa Park",
			PublishedAt: now.AddDate(0, 0, -20),
			Order:       5,
		},
		{
			ID:          "article_006",
			Title:       "The Truth About Carbs: Good vs. Bad",
			Category:    "Nutrition Basics",
			ReadTime:    "5 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800",
			Content:     "Not all carbohydrates are created equal. Understanding the difference can help you make better food choices. **Simple carbs (bad):** Found in white bread, pastries, soda, and candy. They cause rapid blood sugar spikes followed by crashes, leading to hunger and energy dips. **Complex carbs (good):** Found in whole grains, vegetables, legumes, and fruits. They digest slowly, providing sustained energy and keeping you fuller longer. **Fiber matters:** Complex carbs are high in fiber, which aids digestion, feeds beneficial gut bacteria, and helps control blood sugar. Aim for 25-35g of fiber daily. **Glycemic index:** Choose low-GI foods (below 55) like oats, quinoa, and sweet potatoes over high-GI foods (above 70) like white rice and white bread. **Portion control:** Even healthy carbs should be portioned appropriately. A serving is about 1/2 cup cooked grains or 1 medium fruit. **Bottom line:** Don't fear carbs - choose quality sources and pair them with protein and healthy fats for balanced meals.",
			Author:      "Nutritionist Emma Davis",
			PublishedAt: now.AddDate(0, 0, -18),
			Order:       6,
		},
		{
			ID:          "article_007",
			Title:       "Building a Balanced Plate: The Simple Guide",
			Category:    "Meal Planning",
			ReadTime:    "3 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
			Content:     "Creating a balanced plate is easier than you think. Follow this simple visual guide for every meal: **Half your plate (50%):** Non-starchy vegetables like broccoli, spinach, carrots, peppers, tomatoes. These provide vitamins, minerals, and fiber with minimal calories. **Quarter of your plate (25%):** Lean protein such as chicken, fish, tofu, legumes, or eggs. This supports muscle maintenance and keeps you satisfied. **Quarter of your plate (25%):** Complex carbohydrates like brown rice, quinoa, sweet potato, or whole grain bread. These provide sustained energy. **Add healthy fats:** A small portion of nuts, avocado, olive oil, or seeds. **Don't forget:** Stay hydrated with water and include a serving of fruit or dairy if it fits your plan. **Portion sizes:** Use your hand as a guide - protein should be palm-sized, carbs should fit in a cupped hand, and fats should be thumb-sized. This method works for weight loss, maintenance, or muscle gain - simply adjust portions based on your goals.",
			Author:      "Dr. James Rodriguez",
			PublishedAt: now.AddDate(0, 0, -15),
			Order:       7,
		},
		{
			ID:          "article_008",
			Title:       "Sleep and Nutrition: The Overlooked Connection",
			Category:    "Wellness",
			ReadTime:    "5 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1541480551145-2370a440d585?w=800",
			Content:     "Quality sleep is just as important as diet and exercise for your health. **How sleep affects nutrition:** Poor sleep increases ghrelin (hunger hormone) and decreases leptin (satiety hormone), making you hungrier and more likely to overeat. It also impairs glucose metabolism and insulin sensitivity, potentially leading to weight gain. Studies show that people who sleep less than 7 hours consume 385 more calories the next day, often from high-carb, high-fat foods. **How nutrition affects sleep:** Avoid caffeine 6+ hours before bed. Limit alcohol - it disrupts REM sleep. Don't eat large meals 2-3 hours before bed. Try sleep-promoting foods like kiwi, tart cherry juice, fatty fish, or a small portion of complex carbs. **The optimal routine:** Maintain consistent sleep schedule (7-9 hours). Create a dark, cool bedroom environment. Avoid screens 1 hour before bed. Consider magnesium-rich foods like almonds or dark chocolate. **Bottom line:** Prioritize sleep as part of your health journey - it's when your body recovers, repairs, and regulates hormones.",
			Author:      "Dr. Rachel Kim",
			PublishedAt: now.AddDate(0, 0, -12),
			Order:       8,
		},
		{
			ID:          "article_009",
			Title:       "Understanding Food Labels: What to Look For",
			Category:    "Nutrition Basics",
			ReadTime:    "6 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1556656793-08538906a9f8?w=800",
			Content:     "Learning to read food labels empowers you to make informed choices. **Serving size:** This is the foundation - all nutritional info is based on this amount. Check if the package contains multiple servings. **Calories:** Consider your daily needs. Remember that 'low calorie' is 40 or less per serving, 'moderate' is 100-400, and 'high' is 400+. **Macronutrients:** Check protein, carbs, and fat. Look at fiber (aim for 3g+ per serving) and sugar (avoid added sugars when possible). **Ingredients list:** Items are listed by weight, largest first. If sugar appears in the first 3 ingredients, it's a high-sugar food. Watch for hidden sugars: high fructose corn syrup, dextrose, maltose. **Red flags:** Trans fats (partially hydrogenated oils), excessive sodium (above 20% DV per serving), artificial colors and preservatives. **Good signs:** Whole grains listed first, short ingredient lists, recognizable foods. **% Daily Value:** 5% or less is low, 20% or more is high. Use this for nutrients like fiber (want high) and sodium (want low).",
			Author:      "Dietitian Sophie Anderson",
			PublishedAt: now.AddDate(0, 0, -10),
			Order:       9,
		},
		{
			ID:          "article_010",
			Title:       "The Benefits of Eating More Vegetables",
			Category:    "Healthy Eating",
			ReadTime:    "4 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
			Content:     "Vegetables are nutritional powerhouses that should form the foundation of your diet. **Why vegetables are essential:** Rich in vitamins, minerals, antioxidants, and fiber. Low in calories but high in volume, helping you feel full. Reduce risk of heart disease, diabetes, and certain cancers. Support digestive health and immune function. **How many do you need?** Aim for 5-9 servings daily (1 serving = 1 cup raw or 1/2 cup cooked). Include variety: dark leafy greens (spinach, kale), cruciferous (broccoli, cauliflower), colorful (peppers, tomatoes), and root vegetables (carrots, beets). **Easy ways to eat more:** Start meals with a salad. Add vegetables to smoothies (spinach, cucumber). Spiralize vegetables as pasta alternatives. Keep pre-cut veggies for snacks. Roast a big batch for the week. Add vegetables to eggs, sandwiches, and wraps. **Pro tips:** Frozen vegetables are just as nutritious as fresh. Try new cooking methods - roasting brings out sweetness. Season with herbs and spices for flavor without calories. Remember: eat the rainbow for maximum nutrient diversity.",
			Author:      "Chef Maria Garcia",
			PublishedAt: now.AddDate(0, 0, -8),
			Order:       10,
		},
		{
			ID:          "article_011",
			Title:       "Intermittent Fasting: Is It Right for You?",
			Category:    "Weight Management",
			ReadTime:    "7 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800",
			Content:     "Intermittent fasting (IF) has gained popularity as an eating pattern that cycles between periods of eating and fasting. **Common methods:** 16/8 (fast 16 hours, eat within 8-hour window), 5:2 (eat normally 5 days, restrict to 500-600 calories 2 days), or alternate day fasting. **Potential benefits:** Weight loss through calorie restriction, improved insulin sensitivity, enhanced autophagy (cellular cleanup), possible longevity benefits. Some report better focus and energy. **How it works:** During fasting, insulin levels drop, prompting fat burning. Growth hormone increases, preserving muscle mass. **Who should try it:** Generally healthy adults looking for a structured eating pattern. Not suitable for pregnant/nursing women, those with eating disorders, or certain medical conditions. **Tips for success:** Start gradually with 12-hour fasts. Stay hydrated during fasting periods. Don't overeat during eating windows. Focus on nutrient-dense foods. Listen to your body. **Bottom line:** IF is a tool, not magic. It works primarily through calorie restriction. Success depends on what and how much you eat during eating windows.",
			Author:      "Dr. Alex Thompson",
			PublishedAt: now.AddDate(0, 0, -5),
			Order:       11,
		},
		{
			ID:          "article_012",
			Title:       "Healthy Snacking: Smart Choices Between Meals",
			Category:    "Healthy Eating",
			ReadTime:    "5 min read",
			ImageUrl:    "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=800",
			Content:     "Snacking can support your health goals when done right. **Why snack?** Prevents extreme hunger that leads to overeating. Maintains stable blood sugar and energy levels. Provides opportunities for extra nutrients. **Smart snacking rules:** Aim for 150-200 calories per snack. Include protein and/or fiber for satiety. Avoid mindless eating - portion snacks in advance. **Best snack combinations:** Apple with almond butter (fiber + healthy fat), Greek yogurt with berries (protein + antioxidants), hummus with vegetables (protein + fiber), handful of nuts and dried fruit (healthy fats + energy), hard-boiled eggs (protein powerhouse), cottage cheese with tomatoes (protein + vitamins). **Snacks to limit:** Chips, cookies, candy, and other processed foods high in refined carbs, sugar, and unhealthy fats. These cause blood sugar spikes and crashes. **Timing matters:** Snack 2-3 hours after meals if needed. Don't snack within 2 hours of bedtime. **Listen to your body:** Only snack when genuinely hungry, not out of boredom or stress. Sometimes thirst masquerades as hunger - try water first.",
			Author:      "Nutritionist David Lee",
			PublishedAt: now.AddDate(0, 0, -3),
			Order:       12,
		},
	}
}
