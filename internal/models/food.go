package models

import "time"

// Food represents a food item in the database
type Food struct {
	ID               string       `json:"id" firestore:"id"`
	Name             string       `json:"name" firestore:"name"`
	NameIndonesian   string       `json:"nameIndonesian" firestore:"nameIndonesian"`
	Category         string       `json:"category" firestore:"category"` // e.g., "fruits", "vegetables", "grains", "protein", "dairy"
	Nutrition        Nutrition    `json:"nutrition" firestore:"nutrition"`
	ServingSize      ServingSize  `json:"servingSize" firestore:"servingSize"`
	Barcode          string       `json:"barcode,omitempty" firestore:"barcode,omitempty"`
	ImageURL         string       `json:"imageUrl,omitempty" firestore:"imageUrl,omitempty"`
	IsVerified       bool         `json:"isVerified" firestore:"isVerified"`
	Source           string       `json:"source" firestore:"source"` // "system", "user", "openfoodfacts"
	CreatedAt        time.Time    `json:"createdAt" firestore:"createdAt"`
}

// Nutrition represents nutritional information per serving
type Nutrition struct {
	Calories int     `json:"calories" firestore:"calories"` // kcal
	Protein  float64 `json:"protein" firestore:"protein"`   // grams
	Carbs    float64 `json:"carbs" firestore:"carbs"`       // grams
	Fat      float64 `json:"fat" firestore:"fat"`           // grams
	Fiber    float64 `json:"fiber" firestore:"fiber"`       // grams
	Sugar    float64 `json:"sugar" firestore:"sugar"`       // grams
	Sodium   float64 `json:"sodium" firestore:"sodium"`     // mg
}

// ServingSize represents the serving size information
type ServingSize struct {
	Amount float64 `json:"amount" firestore:"amount"` // e.g., 100
	Unit   string  `json:"unit" firestore:"unit"`     // e.g., "g", "ml", "piece", "cup"
}

// FoodSearchRequest represents the query parameters for searching foods
type FoodSearchRequest struct {
	Query    string `form:"q"`                                    // search query
	Category string `form:"category"`                             // filter by category
	Limit    int    `form:"limit,default=20" binding:"max=100"`   // max results
	Offset   int    `form:"offset,default=0" binding:"min=0"`     // pagination offset
}

// CreateFoodRequest represents the request body for creating a food
type CreateFoodRequest struct {
	Name           string      `json:"name" binding:"required"`
	NameIndonesian string      `json:"nameIndonesian" binding:"required"`
	Category       string      `json:"category" binding:"required"`
	Nutrition      Nutrition   `json:"nutrition" binding:"required"`
	ServingSize    ServingSize `json:"servingSize" binding:"required"`
	Barcode        string      `json:"barcode,omitempty"`
	ImageURL       string      `json:"imageUrl,omitempty"`
}

// FoodResponse represents the response for food endpoints
type FoodResponse struct {
	ID             string      `json:"id"`
	Name           string      `json:"name"`
	NameIndonesian string      `json:"nameIndonesian"`
	Category       string      `json:"category"`
	Nutrition      Nutrition   `json:"nutrition"`
	ServingSize    ServingSize `json:"servingSize"`
	Barcode        string      `json:"barcode,omitempty"`
	ImageURL       string      `json:"imageUrl,omitempty"`
	IsVerified     bool        `json:"isVerified"`
	Source         string      `json:"source"`
	CreatedAt      time.Time   `json:"createdAt"`
}

// ToResponse converts Food to FoodResponse
func (f *Food) ToResponse() *FoodResponse {
	return &FoodResponse{
		ID:             f.ID,
		Name:           f.Name,
		NameIndonesian: f.NameIndonesian,
		Category:       f.Category,
		Nutrition:      f.Nutrition,
		ServingSize:    f.ServingSize,
		Barcode:        f.Barcode,
		ImageURL:       f.ImageURL,
		IsVerified:     f.IsVerified,
		Source:         f.Source,
		CreatedAt:      f.CreatedAt,
	}
}

// FoodSearchResponse represents the paginated search results
type FoodSearchResponse struct {
	Foods      []*FoodResponse `json:"foods"`
	Total      int             `json:"total"`
	Limit      int             `json:"limit"`
	Offset     int             `json:"offset"`
	HasMore    bool            `json:"hasMore"`
}
