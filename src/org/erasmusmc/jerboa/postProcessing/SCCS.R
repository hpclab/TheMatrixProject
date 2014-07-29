setwd("/home/schuemie/Research/Jerboa")
require(survival)
analysisData <- read.table("SCCSAnalysisData.txt",header=TRUE,sep=",")

count <- length(analysisData$Filename)
coef <- vector(length = count)
ci95down <- vector(length = count)
ci95up <- vector(length = count)
p <- vector(length = count)
rankP <- vector(length = count)
events <- vector(length = count)
exposure <- vector(length = count)
nonExposure <- vector(length = count)

for (i in 1 : count){
	data <- read.table(as.character(analysisData$Filename[i]),header=TRUE,sep=",")
	#data$AgeRange <- as.factor(data$AgeRange)
	#data$Month <- as.factor(data$Month)
	data$Exposed <- as.factor(data$Exposed)
	events[i] <- sum(data$Event)
	exposure[i] <- sum(data$Duration[data$Exposed == 1])
	nonExposure[i] <- sum(data$Duration[data$Exposed == 0])
	#if (class(try(mod <- clogit(Event ~ Exposed + AgeRange + strata(PatientID) + offset(LogDuration), data = data),silent = FALSE))[1]=="try-error"){
	if (class(try(mod <- clogit(Event ~ Exposed + strata(PatientID) + offset(LogDuration), data = data),silent = FALSE))[1]=="try-error"){
	#if (class(try(mod <- clogit(Event ~ Exposed + Month + strata(PatientID) + offset(LogDuration), data = data),silent = FALSE))[1]=="try-error"){		
		coef[i] = NA
		p[i] = NA
		rankP[i] = 2
		ci95down[i] = NA
		ci95up[i] = NA
	} else {
		coef[i] <- summary(mod)$coef[, "coef"]
		p[i] <- summary(mod)$coef[, "Pr(>|z|)"]
		ci95down[i] = summary(mod)$conf.int[, "lower .95"]
		ci95up[i] = summary(mod)$conf.int[, "upper .95"]
		
		if (is.na(p[i])){
			rankP[i] = 2
		} else if (exp(coef[i]) > 1){
			rankP[i] <- p[i]
		} else { 
			rankP[i] <- 2-p[i]
		}
	}  
}


analysisData$Filename <- NULL
analysisData$Events <- events
analysisData$Exposure <- exposure
analysisData$NonExposure <- nonExposure
analysisData$Beta <- coef
analysisData$p <- p
analysisData$ExpBeta <- exp(coef)
analysisData$CI95Down <- ci95down
analysisData$CI95Up <- ci95up
analysisData$rankP <- rankP
write.csv(analysisData,file="SCCSresults.csv", row.names = FALSE)
