# TODO: Add comment
# 
# Author: schuemie
###############################################################################

folder <- "/data/OSIM/Balanced/"
folder <- commandArgs()[5]
data <- read.table(paste(folder,"Prescriptionstartprofiling.txt", sep=""),header=TRUE,sep=",")
startCol <- which(colnames(data) == "Datapoint...25.days.")
midCol <- which(colnames(data) == "Datapoint..0.days.")
endCol <- which(colnames(data) == "Datapoint..25.days.")

n01 <- rowSums(data[startCol:(midCol-1)])
n11 <- rowSums(data[midCol:endCol])

count <- length(n11)
BinomP <- vector(length = count)
for (p in 1 : count){
	BinomP[p] <- pbinom(n11[p]-1,n11[p]+n01[p],0.5,lower.tail=FALSE);
}
data$LEOPARDP <- BinomP
write.csv(data,file=paste(folder,"SignalsWithLEOPARD.txt", sep=""))





folder <- "/home/data/Pooled/Gold/FixLEOPARD/"
data <- read.table(paste(folder,"PooledStartProfilesLevel4.csv", sep=""),header=TRUE,sep=",")
startCol <- which(colnames(data) == "Datapoint...25.days.")
midCol <- which(colnames(data) == "Datapoint..0.days.")
endCol <- which(colnames(data) == "Datapoint..25.days.")

n01 <- rowSums(data[(startCol+1):(midCol-1)])
n11 <- rowSums(data[(midCol):(endCol-2)])

count <- length(n11)
BinomP <- vector(length = count)
for (p in 1 : count){
	BinomP[p] <- pbinom(n11[p]-1,n11[p]+n01[p],0.5,lower.tail=FALSE);
}
data$LEOPARDP <- BinomP
write.csv(data,file=paste(folder,"LEOPARDLevel4fixed.csv", sep=""),row.names=FALSE)