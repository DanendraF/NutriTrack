package handlers

import (
	"net/http"
	"time"

	"github.com/nutritrack/backend/internal/repository"

	"github.com/gin-gonic/gin"
)

type DailyLogHandler struct {
	mealRepo *repository.MealRepository
}

func NewDailyLogHandler(mealRepo *repository.MealRepository) *DailyLogHandler {
	return &DailyLogHandler{
		mealRepo: mealRepo,
	}
}

// GetDailyLogs gets daily logs for a date range
func (h *DailyLogHandler) GetDailyLogs(c *gin.Context) {
	userID, _ := c.Get("userID")

	// Get query parameters
	startDate := c.Query("start_date")
	endDate := c.Query("end_date")

	// Default to last 7 days if not specified
	if startDate == "" {
		startDate = time.Now().AddDate(0, 0, -7).Format("2006-01-02")
	}
	if endDate == "" {
		endDate = time.Now().Format("2006-01-02")
	}

	dailyLogs, err := h.mealRepo.GetDailyLogs(c.Request.Context(), userID.(string), startDate, endDate)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch daily logs"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"logs": dailyLogs,
		"count": len(dailyLogs),
	})
}

// GetDailyLog gets the daily log for a specific date
func (h *DailyLogHandler) GetDailyLog(c *gin.Context) {
	userID, _ := c.Get("userID")
	date := c.Param("date")

	// Default to today if date not provided
	if date == "" || date == "today" {
		date = time.Now().Format("2006-01-02")
	}

	dailyLog, err := h.mealRepo.GetDailyLog(c.Request.Context(), userID.(string), date)
	if err != nil {
		// Log detailed error for debugging
		c.Error(err) // This will be logged by Gin logger
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to fetch daily log",
			"details": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, dailyLog)
}
