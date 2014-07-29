setwd("/home/schuemie/Research/Jerboa")
require(survival)
analysisData <- read.table("SCCSAnalysisData.txt",header=TRUE,sep=",")
results <- NULL
for (i in 1 : length(analysisData$Filename)){
  data <- read.table(as.character(analysisData$Filename[i]),header=TRUE,sep=",")
  L <- length(colnames(data))
  #formula <- as.formula(paste("Event ~ ", paste(colnames(data)[4:(L-3)], collapse= "+"),"+ AgeRange + strata(PatientID) + offset(LogDuration)"))
  formula <- as.formula(paste("Event ~ ", paste(colnames(data)[4:(L-3)], collapse= "+"),"+ strata(PatientID) + offset(LogDuration)"))
  mod <- clogit(formula, data = data)
  resultsThisEvent <- data.frame(ATC = rownames(summary(mod)$coef[1:(L-6), 0]), EventType = rep(analysisData$EventType[i],L-6),Beta = summary(mod)$coef[1:(L-6), "coef"], p = summary(mod)$coef[1:(L-6), "Pr(>|z|)"])
  results <- rbind (results, resultsThisEvent)
}  
write.csv(results,file="SCCSresults.csv")    
