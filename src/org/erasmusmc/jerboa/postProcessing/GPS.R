# 
# Author: schuemie
###############################################################################

folder <- commandArgs()[5]
sourcedata <- read.table(paste(folder,"RelativeRisks.csv", sep=""),header=TRUE,sep=",")
data <- data.frame(sourcedata$ATC,sourcedata$EventType,sourcedata$Events)
colnames(data) <- c("drug","event","count")

data <- data[data$count > 0,]
transform <- function (DATA.FRAME, MARGIN.THRES = 1) 
{
	data <- DATA.FRAME
	data[, 1] <- as.factor(DATA.FRAME[, 1])
	data[, 2] <- as.factor(DATA.FRAME[, 2])
	data[, 3] <- as.double(DATA.FRAME[, 3])
	coln <- names(data)
	names(data)[3] <- "n11"
	data_cont <- xtabs(n11 ~ ., data = data)
	n1._mat <- apply(data_cont, 1, sum)
	n.1_mat <- apply(data_cont, 2, sum)
	if (MARGIN.THRES > 1) {
		while (sum(n1._mat < MARGIN.THRES) > 0 | sum(n.1_mat < 
						MARGIN.THRES) > 0) {
			data_cont <- data_cont[n1._mat >= MARGIN.THRES, ]
			data_cont <- data_cont[, n.1_mat >= MARGIN.THRES]
			n1._mat <- apply(data_cont, 1, sum)
			n.1_mat <- apply(data_cont, 2, sum)
		}
	}
	coord <- which(data_cont != 0, arr.ind = TRUE)
	coord <- coord[order(coord[, 1]), ]
	Nb_n1. <- length(n1._mat)
	Nb_n.1 <- length(n.1_mat)
	libel.medoc <- rownames(data_cont)[coord[, 1]]
	libel.effet <- colnames(data_cont)[coord[, 2]]
	n11 <- data_cont[coord]
	N <- sum(n11)
	n1. <- n1._mat[coord[, 1]]
	n.1 <- n.1_mat[coord[, 2]]
	RES <- vector(mode = "list")
	RES$L <- data.frame(libel.medoc, libel.effet)
	colnames(RES$L) <- coln[1:2]
	RES$data <- cbind(n11, n1., n.1)
	rownames(RES$data) <- paste(libel.medoc, libel.effet)
	RES$N <- N
	RES
}
DATABASE <- transform(data)


DATA <- DATABASE$data
N <- DATABASE$N
L <- DATABASE$L     
PRIOR.INIT = c(alpha1= 0.2, beta1= 0.06, alpha2=1.4, beta2=1.8, w=0.1)
RR0 = 1
RANKSTAT = 1
DECISION = 1
DECISION.THRES = 0.05

n11 <- DATA[,1] # les nij ou n11 (c'est pareil)
n1. <- DATA[,2] # les marges lignes (effets ind�sirables)
n.1 <- DATA[,3] # les marges colonnes (m�dicaments)
E <- DATA[,2] * DATA[,3] / N # les effectifs attendus      

P_OUT <- TRUE

f<-function(p,n11,E){
	sum(-log((p[5] * dnbinom(n11, size=p[1], prob=p[2]/(p[2]+E)) + (1-p[5]) * dnbinom(n11, size=p[3], prob=p[4]/(p[4]+E)))))
}

# Dumouchel situation (we will consider the whole contingency table with 0)
data_cont<-xtabs(DATA[,1]~L[,1]+L[,2])
n1._mat <- apply(data_cont,1, sum) # on recalcule les marges des lignes...
n.1_mat <- apply(data_cont,2, sum) # ...et des colonnes
n1._c <- rep(n1._mat, times=length(n.1_mat))
n.1_c <- rep(n.1_mat, each=length(n1._mat))
E_c <- n1._c * n.1_c / N
n11_c <- as.vector(data_cont)

p_out <-nlm(f, p=PRIOR.INIT, n11=n11_c, E=E_c, iterlim=500)



PRIOR.PARAM <-p_out$estimate
code.convergence <- p_out$code
Nb.Cell <- length(n11)
post.H0 <- vector(length=Nb.Cell)


# Posterior probability of the null hypothesis
Q <- PRIOR.PARAM[5]*dnbinom(n11,size=PRIOR.PARAM[1],prob=PRIOR.PARAM[2]/(PRIOR.PARAM[2]+E)) / 
		(PRIOR.PARAM[5]*dnbinom(n11,size=PRIOR.PARAM[1],prob=PRIOR.PARAM[2]/(PRIOR.PARAM[2]+E)) + (1-PRIOR.PARAM[5])* 
			dnbinom(n11,size=PRIOR.PARAM[3],prob=PRIOR.PARAM[4]/(PRIOR.PARAM[4]+E)))

post.H0 <- Q*pgamma(RR0,PRIOR.PARAM[1]+n11,PRIOR.PARAM[2]+E) +(1-Q)*pgamma(RR0,PRIOR.PARAM[3]+n11,PRIOR.PARAM[4]+E) # proba a posteriori de H0

# Posterior Expectation of Lambda
postE <- log(2)^(-1)*(Q*(digamma(PRIOR.PARAM[1]+n11)-log(PRIOR.PARAM[2]+E))+(1-Q)*(digamma(PRIOR.PARAM[3]+n11)-log(PRIOR.PARAM[4]+E)))


# Algorithme allowing the calculation of the CI Lower Bound (at Seuil%)

# Algorithm Emmanuel Roux
QuantileDuMouchel<-function(Seuil,Q,a1,b1,a2,b2) {
	m<-rep(-100000,length(Q))
	M<-rep(100000,length(Q))
	x<-rep(1,length(Q))
	Cout<-FCoutQuantileDuMouchel(x,Seuil,Q,a1,b1,a2,b2)
	while (max(round(Cout*1e4))!=0)	{
		S<-sign(Cout)
		xnew<-(1+S)/2*((x+m)/2)+(1-S)/2*((M+x)/2)
		M<-(1+S)/2*x+(1-S)/2*M
		m<-(1+S)/2*m+(1-S)/2*x
		x<-xnew
		Cout<-FCoutQuantileDuMouchel(x,Seuil,Q,a1,b1,a2,b2)
	}
	x
}
FCoutQuantileDuMouchel<-function(p,Seuil,Q,a1,b1,a2,b2) {
	Q*pgamma(p,shape=a1,rate=b1)+(1-Q)*pgamma(p,shape=a2,rate=b2)-Seuil
}

# Calculation of the Lower Bound. (VALUEbis is former LBQ)
LB <- QuantileDuMouchel(0.025,Q,PRIOR.PARAM[1]+n11,PRIOR.PARAM[2]+E,PRIOR.PARAM[3]+n11,PRIOR.PARAM[4]+E)
UB <- QuantileDuMouchel(0.975,Q,PRIOR.PARAM[1]+n11,PRIOR.PARAM[2]+E,PRIOR.PARAM[3]+n11,PRIOR.PARAM[4]+E)

############################ Output #############################
output <- data.frame(L[,1],L[,2],post.H0,postE,2^postE,LB,UB)
colnames(output) <- c("ATC","EventType","postH0","postE","RR","CI95down","CI95up")
write.csv(output,file=paste(folder,"SignalsWithGPS.csv", sep=""),row.names = FALSE)