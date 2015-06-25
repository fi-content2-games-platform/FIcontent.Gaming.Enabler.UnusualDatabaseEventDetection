# FIcontent.Gaming.Enabler.UnusualDatabaseEventDetection
Unusual Database-Event Detection SE

## Installation and Setup

### Requirements
- Java runtime
- A database (only MySQL is currently supported)

### Copy files

### Edit Configuration
- edit the `bin/config.properties` file (see comments in the example file)
- create a rules file or modify the `bin/leaderboard-rules.txt` example

This is a small rules file with only two rules:
```
max(augmentedresistance highscore)
SELECT MAX(highscore) FROM mygame.augmentedresistance
0
90000
min(augmentedresistance highscore)
SELECT MIN(highscore) FROM mygame.augmentedresistance
0
90000
max(gnometrader highscore)
```

### Installation as service
You probably want this to run as a service, see [installation-as-service-in_etc-init.d](installation-as-service-in_etc-init.d) subfolder.
