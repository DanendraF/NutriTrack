package main

import (
	"context"
	"fmt"
	"log"

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

	fmt.Println("🌱 Initializing Tips collection...")

	tips := []map[string]interface{}{
		{
			"id":          "tip_1",
			"title":       "Minum Air Putih yang Cukup",
			"description": "Pastikan kamu minum minimal 8 gelas air putih (2 liter) setiap hari. Air membantu metabolisme tubuh dan menjaga kesehatan kulit.",
			"category":    "hydration",
			"icon":        "water_drop",
			"order":       1,
		},
		{
			"id":          "tip_2",
			"title":       "Sarapan adalah Kunci",
			"description": "Jangan skip sarapan! Sarapan membantu meningkatkan metabolisme dan memberi energi untuk memulai hari.",
			"category":    "nutrition",
			"icon":        "breakfast_dining",
			"order":       2,
		},
		{
			"id":          "tip_3",
			"title":       "Porsi Makan yang Seimbang",
			"description": "Gunakan metode piring: 1/2 sayuran, 1/4 protein, 1/4 karbohidrat kompleks. Ini membantu mengontrol kalori dan nutrisi.",
			"category":    "nutrition",
			"icon":        "restaurant",
			"order":       3,
		},
		{
			"id":          "tip_4",
			"title":       "Olahraga 30 Menit Sehari",
			"description": "Lakukan aktivitas fisik minimal 30 menit setiap hari. Bisa jalan kaki, jogging, atau workout di rumah.",
			"category":    "exercise",
			"icon":        "directions_run",
			"order":       4,
		},
		{
			"id":          "tip_5",
			"title":       "Tidur 7-8 Jam",
			"description": "Tidur yang cukup sangat penting untuk pemulihan tubuh dan mengatur hormon lapar. Kurang tidur bisa meningkatkan nafsu makan.",
			"category":    "sleep",
			"icon":        "bedtime",
			"order":       5,
		},
		{
			"id":          "tip_6",
			"title":       "Kurangi Gula dan Garam",
			"description": "Batasi konsumsi gula tambahan maksimal 4 sendok makan per hari dan garam maksimal 1 sendok teh per hari.",
			"category":    "nutrition",
			"icon":        "block",
			"order":       6,
		},
		{
			"id":          "tip_7",
			"title":       "Makan Buah dan Sayur",
			"description": "Konsumsi minimal 5 porsi buah dan sayur setiap hari. Kaya akan vitamin, mineral, dan serat yang baik untuk tubuh.",
			"category":    "nutrition",
			"icon":        "eco",
			"order":       7,
		},
		{
			"id":          "tip_8",
			"title":       "Protein di Setiap Waktu Makan",
			"description": "Pastikan ada sumber protein (telur, ayam, ikan, tahu, tempe) di setiap waktu makan untuk menjaga massa otot.",
			"category":    "nutrition",
			"icon":        "egg",
			"order":       8,
		},
		{
			"id":          "tip_9",
			"title":       "Hindari Makan Larut Malam",
			"description": "Usahakan makan malam 3 jam sebelum tidur. Makan larut malam bisa mengganggu kualitas tidur dan pencernaan.",
			"category":    "habits",
			"icon":        "schedule",
			"order":       9,
		},
		{
			"id":          "tip_10",
			"title":       "Catat Makananmu",
			"description": "Rajin mencatat makanan yang kamu konsumsi membantu meningkatkan kesadaran pola makan dan mencapai target nutrisi.",
			"category":    "habits",
			"icon":        "edit_note",
			"order":       10,
		},
	}

	batch := client.Batch()
	for _, tip := range tips {
		tipID := tip["id"].(string)
		docRef := client.Collection("tips").Doc(tipID)
		batch.Set(docRef, tip)
		fmt.Printf("  ✓ Adding tip: %s\n", tip["title"])
	}

	_, err = batch.Commit(ctx)
	if err != nil {
		log.Fatalf("Failed to commit tips: %v", err)
	}

	fmt.Printf("\n✅ Successfully created %d tips in Firestore!\n", len(tips))
}
