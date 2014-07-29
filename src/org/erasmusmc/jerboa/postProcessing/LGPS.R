# TODO: Add comment
# 
# Author: schuemie
###############################################################################

folder <- commandArgs()[5]
data <- read.table(paste(folder,"RelativeRisks.csv", sep=""),header=TRUE,sep=",")
data <- data[data$expected > 0,]

L <- data[,c("ATC","EventType")]
n11 <- data$Events
E <- data$expected

PRIOR.INIT = c(alpha1= 0.2, beta1= 0.06, alpha2=1.4, beta2=1.8, w=0.1)
RR0 = 1
RANKSTAT = 1
DECISION = 1
DECISION.THRES = 0.05

f<-function(p,n11,E){
	sum(-log((p[5] * dnbinom(n11, size=p[1], prob=p[2]/(p[2]+E)) + (1-p[5]) * dnbinom(n11, size=p[3], prob=p[4]/(p[4]+E)))))
}

p_out <-nlm(f, p=PRIOR.INIT, n11=n11, E=E, iterlim=500)
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

data$postH0 <- post.H0
data$postLogE <- postE
data$postE <- 2^postE
data$CI95down <- LB
data$CI95up <- UB

write.csv(data,file=paste(folder,"SignalsWithLGPS.csv", sep=""),row.names = FALSE)