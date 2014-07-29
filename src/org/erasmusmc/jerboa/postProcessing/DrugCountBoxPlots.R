# TODO: Add comment
# 
# Author: schuemie
###############################################################################

setwd("x:/")
data <- read.table("CCresultsAllSignalsDC.csv",header=TRUE,sep=",")
boxplot(DrugCountBeta~EventType, data = data,xlab="Event type", ylab="Drug Count coefficient")
