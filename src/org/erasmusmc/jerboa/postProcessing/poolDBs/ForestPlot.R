require(ggplot2)
setwd("/home/temp/")
data <- read.table("data2.csv",header=TRUE,sep=",")

breaks <- c(0.5,1,2,3,4,5,10,25,50,100) 


ggplot(data, aes(x=Order, y=OR, ymin=Lower, ymax=Upper,colour=Type)) +
  geom_hline(aes(yintercept=breaks), colour ="#DDDDDD", lty=1)+
  geom_pointrange() +
  scale_y_continuous(trans="log10", limits=c(0.08,60), breaks=breaks, labels = breaks) +
  scale_x_continuous(breaks=data$Order,labels = data$Country, trans = 'reverse') +
  opts(panel.grid.minor = theme_blank()) +
  opts(panel.background= theme_blank()) +
  opts(panel.grid.major= theme_blank()) +
  opts(axis.ticks = theme_blank()) +
  opts(legend.key= theme_blank()) +
  coord_flip() + 
  scale_colour_manual(values = c("#888888","black")) +
  geom_hline(aes(yintercept=1), lty=2)+ xlab('Country') 
 