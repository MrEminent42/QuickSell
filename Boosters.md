# Boosters in QuickSell
A new system of boosters called Percentage Boosters was added in QuickSell 2.1. Read more below to understand the difference between
the original system and the new Percentage Boosters system, as well as **what to do before upgrading**.

If you know what the difference is and want to switch, check out [How to Switch](#how-to-switch).

## Original Boosters

#### Calculation
When you sell items with Original Boosters, the worth of the items is multiplied by each booster you have.
The product of that is then added to your balance. 

Math:
`total = worth + (worth * booster1 * booster2 * booster3)...`

For example: `100 + (100 * 2.0 * 1.5)`<br>
Here, a player has a 2.0x booster and a 1.5x booster.

#### Commands
Original Boosters are issued using:<br>
`/[p]booster <type> <name of player> <multiplier> <duration>`<br>
`/[p]booster monetary PixelSquared 1.5 15` would give player PixelSquared a 1.5x booster for 15 minutes.

## Percentage Boosters

#### Calculation
Percentage Boosters give you a bonus percentage in addition to what the items are worth.

Math: `total = worth + (woth * percentage1) + (worth * percentage2) + (worth * percentage3)...`

For example: `100 + (100 * 0.25) + (100 * 0.05) = $130`<br>
Here, a player has a 25% booster and a 5% booster `(25% = 25/100 = 0.25)`.

#### Commands
Percentage Boosters are issued using:<br>
`/[p]booster <type> <name of player> <boost %> <duration>`<br>
`/[p]booster monetary PixelSquared 20% 15` would give player PixelSquared a 20% bonus for 15 minutes.

## How to Switch

#### Before switching
Make sure you have no current boosters running on your server. After switching, any current boosters will be overpowered since a 2.0x Original Booster will
turn into a 200% Percentage Booster.

#### Switch
In your config.yml, set boosters > `use-percent-logic` to true.

#### After you switch
I recommend changing the some messages in your messages.yml, as they currently have a bunch of 'x's in them.
**Please note: when you are using Percentage Boosters and put %multiplier% in the messages.yml, the '%' will be automatically inserted.**

I recommend changing the following messages:
