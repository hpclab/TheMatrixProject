folder <- commandArgs()[5]
sourcedata <- read.table(paste(folder,"RelativeRisks.csv", sep=""),header=TRUE,sep=",")
data <- data.frame(sourcedata$ATC,sourcedata$EventType,sourcedata$Events)
colnames(data) <- c("drug","event","count")
RR0 = 1
MIN.n11 = 1
DECISION = 1
DECISION.THRES = 0.05
RANKSTAT = 1
NB.MC = 10000
MC = TRUE

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

n11 <- DATA[,1]
n1. <- DATA[,2] # les marges lignes (effets indésirables)
n.1 <- DATA[,3] # les marges colonnes (médicaments)
n10 <- n1. - n11
n01 <- n.1 - n11
n00 <- N - (n11+n10+n01)
E <- n1. * n.1 / N # les counts attendus

if(MIN.n11 > 1) {
	E <- E[n11 >= MIN.n11]
	n1. <- n1.[n11 >= MIN.n11]
	n.1 <- n.1[n11 >= MIN.n11]
	n10 <- n10[n11 >= MIN.n11]
	n01 <- n01[n11 >= MIN.n11]
	n00 <- n00[n11 >= MIN.n11]
	L <- L[n11 >= MIN.n11,]
	n11 <- n11[n11 >= MIN.n11]
}

Nb.Cell <- length(n11)

if (MC == FALSE) {
	post.H0 <- matrix(nrow=Nb.Cell,ncol=length(RR0))
	p1  <- 1 + n1.
	p2  <- 1 + N - n1.
	q1  <- 1 + n.1
	q2  <- 1 + N - n.1
	r1  <- 1 + n11
	r2b <- N - n11 -1 + (2+N)^2/(q1*p1)
	EICb <- log(2)^(-1) * (digamma(r1) - digamma(r1+r2b) - (digamma(p1) - digamma(p1+p2) + digamma(q1) - digamma(q1+q2)))
	VICb <- log(2)^(-2) * (trigamma(r1) - trigamma(r1+r2b) + (trigamma(p1) - trigamma(p1+p2) + trigamma(q1) - trigamma(q1+q2)))
	post.H0 <- pnorm(log(RR0),EICb,sqrt(VICb))
# Calculation of the Lower Bound
	LB <- qnorm(0.025,EICb,sqrt(VICb))
	UB <- qnorm(0.975,EICb,sqrt(VICb))
}

if (MC == TRUE) { # Advanced option MC
	require(MCMCpack)
	n1. <- n11 + n10
	n.1 <- n11 + n01
	Nb_Obs <- length(n11)
	
	## Nouvelles priors
	q1. <- (n1. +.5)/(N +1)
	q.1 <- (n.1 +.5)/(N +1)
	q.0 <- (N - n.1 +.5)/(N +1)
	q0. <- (N - n1. +.5)/(N +1)
	
	a.. <- .5/(q1.*q.1) ## le .5 devrait pouvoir être changé
	
	a11 <- q1.*q.1* a..
	a10 <- q1.*q.0* a..
	a01 <- q0.*q.1* a..
	a00 <- q0.*q.0* a..
	
	g11 <- a11 + n11
	g10 <- a10 + n10
	g01 <- a01 + n01
	g00 <- a00 + n00
	g1. <- g11 + g10
	g.1 <- g11 + g01
	
	post.H0 <- vector(length=length(n11))
	LB <- vector(length=length(n11))
	UB <- vector(length=length(n11))
	quantile <- vector("numeric",length=length(n11))
	for (m in 1 : length(n11)){
		p <- rdirichlet(NB.MC,c(g11[m],g10[m],g01[m],g00[m]))
		p11 <- p[,1]
		p1. <- p11 + p[,2]
		p.1 <- p11 + p[,3]	
		IC_monte <- log(p11/(p1.* p.1))
		temp <- IC_monte < log(RR0)
		post.H0[m] <- sum(temp)/NB.MC
		LB[m] <- sort(IC_monte)[round(NB.MC * 0.025)]
		UB[m] <- sort(IC_monte)[round(NB.MC * 0.975)]
	}
	rm(p11,p1.,p.1,IC_monte,temp)
	gc()
}

data <- data.frame(L[,1],L[,2],n11,(n11/E),post.H0,exp(LB),exp(UB))
colnames(data) <- c("ATC","EventType","Events","RR","p","CI95down","CI95up")
write.csv(data,file=paste(folder,"SignalsWithBCPNN.csv", sep=""),row.names = FALSE)



