# TODO: Add comment
# 
# Author: schuemie
###############################################################################


setwd("/home/schuemie/Research/Jerboa")
require(MCMCpack)
analysisData <- read.table("data.txt",header=TRUE,sep=",")
results <- NULL
for (i in 1 : length(analysisData$Filename)){
	data <- read.table(as.character(analysisData$Filename[i]),header=TRUE,sep=",")
	data$AgeRange <- as.factor(data$AgeRange)
	data$Gender <- as.factor(data$Gender)
	L <- length(colnames(data))
	#formula <- as.formula(paste("Event ~ AgeRange + Gender + ", paste(colnames(data)[5:(L-1)], collapse= "+")))
	formula <- as.formula(paste("Event ~ AgeRange + Gender + DrugCount + ", paste(colnames(data)[5:(L-1)], collapse= "+")))
	mod <- MCMClogit(formula, data = data, tune = 0.15, mcmc=100000)
	#plot(mod)
	stats <- summary(mod)$statistics
	resultsThisEvent <- data.frame(ATC = rownames(stats), EventType = rep(analysisData$EventType[i],length(stats)),Mean = stats[,1],SD = stats[,2],SE1 = stats[,3],SE2 = stats[,4])
	results <- rbind (results, resultsThisEvent)
}  
write.csv(results,file="CCresults.csv", row.names = FALSE)    
