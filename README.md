# Overdexer

An override/ directory indexer for Baldur's Gate (to run some tests on
mod/tooling and see if I can do anything fun and interesting with
parsing the binary formats in Clojure).

# Installation

Ensure you have a current copy of Clojure available (clj command).

Coming soon: An uberjar that'll allow a simple
`java -jar snapshot.jar` to do this.

# Usage

## Indexing

To index your files:

```
make run DIALOG_DIR=/full/path/to/bgee2/lang/en_US OVERRIDE_DIR=/full/path/to/bgee2/override
```

## Querying

Try out some database queries and observe some nice response times:

### Getting the most damaging one handed swords in game/current install

```
❯ time echo "select ih.pkid, (select string from dialog d where d.pkid = ih.identified_name) as name, (dice_thrown + damage_bonus) as min_dmg, (dice_thrown * dice_sides + damage_bonus) as max_dmg, dice_thrown, dice_sides, damage_bonus FROM itm_header ih JOIN itm_ext_header USING (pkid) WHERE pkid like '%sw1h%' and enchantment > 0 AND damage_bonus < 30 ORDER BY (dice_thrown * dice_sides) + damage_bonus DESC LIMIT 10 OFFSET 0;" | sqlite3 ./snapshot.db -table
+--------------+---------------------------+---------+---------+-------------+------------+--------------+
|     pkid     |           name            | min_dmg | max_dmg | dice_thrown | dice_sides | damage_bonus |
+--------------+---------------------------+---------+---------+-------------+------------+--------------+
| sw1h65.itm   | Purifier +5               | 6       | 15      | 1           | 10         | 5            |
| sw1h71.itm   | Hindo's Doom +5           | 6       | 15      | 1           | 10         | 5            |
| sw1hgraz.itm | Bastard Sword +1          | 6       | 15      | 1           | 10         | 5            |
| c2sw1h02.itm | Celestial Fury +5         | 6       | 15      | 1           | 10         | 5            |
| u#sw1h07.itm | Corthala Family Blade +4  | 5       | 14      | 1           | 10         | 4            |
| sw1h64.itm   | Purifier +4               | 5       | 14      | 1           | 10         | 4            |
| sw1h70.itm   | Hindo's Doom +4           | 5       | 14      | 1           | 10         | 4            |
| h_sw1h02.itm | Kerykeion: Stheno's Blade | 4       | 13      | 1           | 10         | 3            |
| s#sw1h01.itm | Sacred Justice            | 6       | 13      | 1           | 8          | 5            |
| bdsw1h05.itm | Gift of the Demon         | 6       | 13      | 1           | 8          | 5            |
+--------------+---------------------------+---------+---------+-------------+------------+--------------+
echo   0.00s user 0.00s system 43% cpu 0.002 total
sqlite3 ./snapshot.db -table  0.01s user 0.01s system 84% cpu 0.017 total
```

# License

Copyright © 2022 Matthew Carter <m@ahungry.com>

Distributed under the Eclipse Public License version 1.0.
