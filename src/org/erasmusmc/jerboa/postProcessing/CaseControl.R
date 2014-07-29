# TODO: Add comment
# 
# Author: schuemie
###############################################################################
setwd("x:/")

require(survival)
analysisData <- read.table("CCAnalysisdata.txt",header=TRUE,sep=",")
#analysisData <- read.table("CCRAnalysisdata.txt",header=TRUE,sep=",")

drugCount <- TRUE
count <- length(analysisData$Filename)
coef <- vector(length = count)
ci95down <- vector(length = count)
ci95up <- vector(length = count)
p <- vector(length = count)
rankP <- vector(length = count)
cases <- vector(length = count)
controls <- vector(length = count)
if (drugCount){
	dcCoef <- vector(length = count)
	dcP <- vector(length = count)
}

for (i in 1 : count){
	data <- read.table(as.character(analysisData$Filename[i]),header=TRUE,sep=",")
	data$Exposed <- as.factor(data$Exposed)
	cases[i] <- sum(data$Event)
	controls[i] <- length(data$Event) - cases[i]
	if (drugCount){
		formula <- Event ~ Exposed + DrugCount + strata(CaseSetID)
	} else {
		formula <- Event ~ Exposed + strata(CaseSetID)
	}
	if (class(try(mod <- clogit(formula, data = data),silent = FALSE))[1]=="try-error"){
		coef[i] = NA
		p[i] = NA
		rankP[i] = 2
		ci95down[i] = NA
		ci95up[i] = NA
		if (drugCount){
			dcCoef[i] = NA
			dcP[i] = NA
		}
	} else {
		coef[i] <- summary(mod)$coef[, "coef"][1]
		p[i] <- summary(mod)$coef[, "Pr(>|z|)"][1]
		ci95down[i] = summary(mod)$conf.int[, "lower .95"]
		ci95up[i] = summary(mod)$conf.int[, "upper .95"]
		if (drugCount){
			dcCoef[i] = summary(mod)$coef[, "coef"][2]
			dcP[i] = summary(mod)$coef[, "Pr(>|z|)"][2]
		}
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
analysisData$Cases <- cases
analysisData$Controls <- controls
analysisData$Beta <- coef
analysisData$p <- p
analysisData$ExpBeta <- exp(coef)
analysisData$CI95Down <- ci95down
analysisData$CI95Up <- ci95up
analysisData$rankP <- rankP
if (drugCount){
	analysisData$DrugCountBeta <- dcCoef
	analysisData$DrugCountP <- dcP
}
write.csv(analysisData,file="CCresults.csv", row.names = FALSE)
#write.csv(analysisData,file="CCRresults.csv", row.names = FALSE)
