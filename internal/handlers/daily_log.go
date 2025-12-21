package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func GetDailyLogs(c *gin.Context) {
	userID, _ := c.Get("userID")

	// TODO: Fetch daily logs from Firestore
	c.JSON(http.StatusOK, gin.H{
		"user_id":    userID,
		"daily_logs": []interface{}{},
		"message":    "Get daily logs - to be implemented",
	})
}

func GetDailyLog(c *gin.Context) {
	userID, _ := c.Get("userID")
	date := c.Param("date")

	// TODO: Fetch daily log for specific date from Firestore
	c.JSON(http.StatusOK, gin.H{
		"user_id": userID,
		"date":    date,
		"message": "Get daily log - to be implemented",
	})
}
