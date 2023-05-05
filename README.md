# Best Steam Trading Card Games Finder (BSTCGF)
   
---
   
![Repo Stats by Repobeats](https://repobeats.axiom.co/api/embed/487fa5f690163154569de26cad1426252c719883.svg "Repobeats analytics image")   
   
---
   
## Description
A small Java programm to get all Steam games with Trading Cards sorted by a rating

---

## Why?
I started using ASF and wanted to get the most cards for the least money spend. I stared comparing game by game but of course it's boring and takes long. 
I couldn't find something like this online but there was nothing.

---

## How?
### API Requests
First I get all games with Trading Cards using an API request from [Steam Card Exchange](https://www.steamcardexchange.net/).   
Now with all IDs I can use [this Steam Web API request](https://github.com/Revadike/InternalSteamWebAPI/wiki/Get-App-Details) to get the price information.   
   
### Rating
As I said I wanted to get the most cards for the lowest game price. It is important to keep in mind that you won't get e.g. 5 out of 5 available Trading Cards, 
you get half of the cards and I case of an odd number it rounds up (so if a game has 5 Trading Cards you can obtain 3 cards, in cas of 7 you will get 4 and so on).
So to get the Rating I used the formula <code>current price / ((available cards / 2) rounded up)</code>. The lower the rating the better is the game to get Trading Cards.   
The table is sorted by rating, going from lowest to highest (best to worst game) and, if multiple games have the same rating, by name.
   
### For more details, check the code
   
---
   
## How to use
Just start the programm, click <code>Load Games</code> and wait a little bit.   
   
The Steam Web API request has a maximum off 200 requests per 5 minutes, right now the programm requires 111 requests 
(there 11.075 Games with Trading Cards at the moment of writing this and each Steam Web API request can have up to 100 IDs). This means you can get the Data once every 5 minutes without getting blocked from the Steam Web API.
   
Don't worry if you get blocked, just wait a few minutes (probably 5, but that's just a guess) or changing your IP e.g. by using a VPN and try again.
   
---
