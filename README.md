# Best Steam Trading Card Games Finder (BSTCGF)
   
---
   
![Repo Stats by Repobeats](https://repobeats.axiom.co/api/embed/487fa5f690163154569de26cad1426252c719883.svg "Repobeats analytics image")   
   
---
   
## Description
A small Java programm to get all Steam games with Trading Cards sorted by a rating

---

## Why?
I started using ASF and wanted to get the most cards for the least money spend. I stared comparing game by game but of course it's boring and takes long. 
I couldn't find something like this online, so I just did it myself.

---

## How?
### API Requests
First I get all games with Trading Cards using an API request from [Steam Card Exchange](https://www.steamcardexchange.net/).   
Now with all IDs I can use [this Steam Web API request](https://github.com/Revadike/InternalSteamWebAPI/wiki/Get-App-Details) to get the price information.   
   
### Rating
As I said I wanted to get the most cards for the lowest game price. It is important to keep in mind that you won't get e.g. 5 out of 5 available Trading Cards, 
you get half of the cards and I case of an odd number it rounds up (so if a game has 5 Trading Cards you can obtain 3 cards, in case of 7 you will get 4 and so on).
So to get the rating I use the formula <code>current price / ((available cards / 2) rounded up)</code>. The lower the rating the better is the game to get Trading Cards.   
The table is sorted by rating, going from lowest to highest (best to worst game) and, if multiple games have the same rating, they are sorted by total number of cards for the game 
(games with fewer cards are above games with more cards because it is easier to get badge). If there are multiple cards with the same rating and total number of cards, 
they are sorted by name.
   
### For more details, check the code

---

## How to use
Just start the programm, click <code>Load Games</code> and watch as the games are loaded.   
   
The Steam Web API request has a maximum off 200 requests per 5 minutes, right now the programm requires 111 requests 
(there 11.075 Games with Trading Cards at the moment of writing this and each Steam Web API request can have up to 100 IDs). This means you can get the data once every 5 minutes without getting blocked from the Steam Web API.
   
Don't worry if you get blocked, just wait a few minutes (probably 5, but that's just a guess) or change your IP e.g. by using a VPN and try again.

If a game is Free2Play, it will be sorted out, because you have to spend ~10$ to get one card. 
The method used for this will also sort out games that are unavailable in your region (because the Steam API will not return price information for those games). 
I believe the blocking depends on your IP address and not on the used country code, but I'm not sure about that.

---

## The branches

### [dev](https://github.com/n0eL1405/BestSteamTradingCardGamesFinder/tree/dev)
Always the latest state of the code. Whenever I stop coding, I will push the code to this branch. The code may or may not work.

### [master](https://github.com/n0eL1405/BestSteamTradingCardGamesFinder/tree/master)
This code is always buildable and runnable. The features in this branch are all working, and you could compile and use the programm from this branch without any problems (except hidden bugs). But there may be features that are useless, because other features are still in development.

### [release](https://github.com/n0eL1405/BestSteamTradingCardGamesFinder/tree/release/v1.0)
All branches in this folder represent a release version of the programm. If you want to check the code of a specific version, you can do it there. Once a branch is created in the folder, it will never be changed (except I find some critical stuff that really shouldn't be public in these branches).

---

### Developed in Java 17, downgraded to Java 11. Currently only tested with Windows 11.