# VEV

> A kitten-herding, queue-management entropy-em-up strategy and automation game.

[Game Homepage](http://tim-martin.co.uk/2021/08/24/vev.html)

[Get it on itch.io](https://timboe.itch.io/vev)

[Get it on Google Play](https://play.google.com/store/apps/details?id=timboe.vev)

VEV is a non-violent strategy and automation game in which you are tasked to clear the land of particles in the shortest possible time.

A fixed number of particles will spawn into the land through white holes which dot the world, your task is to corral the unruly lot into deconstruction facilities which convert the particles into energy and (in some cases) yet more particles, which additionally then need further deconstruction.

There are six deconstruction facilities available, each accepting a different trio of particle types and producing a different amount of energy and output particles for each type. Refineries are in addition to the deconstruction facilities, these will collect ore and provide an additional source of income to get you started. All buildings can be upgraded using energy to increase their throughput.

The main strategy in VEV revolves around a balance between the number of deconstruction facilities, their queue length, upgrade level, and how the facilities are interconnected together to automate particle deconstruction cascades - while also handling the new fresh particles produced by the white holes.

The white holes and all deconstruction facilities can set a destination for each particle type they produce, spawned particles will automatically go to this destination. Deconstruction facilities can additionally specify an overflow location, all particles which try and enter the facilitie's queue when it is full will instead divert to the overflow location. This allows the chaining of a larger number of facilities with shorter queues to improve throughput. But note that cyclic loops are not allowed, if a particle gets redirected back to a facility it has already been rejected from, it will just hang around the entrance to the queue, and probably wander off.

![Screenshot1](https://github.com/timboe/vev/blob/master/promotional/vev_desktop_1.png?raw=true)

![Screenshot2](https://github.com/timboe/vev/blob/master/promotional/vev_desktop_2.png?raw=true)

### Credits

Framework: [libGDX](https://libgdx.badlogicgames.com/).

Ben Ruiji: [A\*](https://gist.github.com/benruijl/3385624).

Buch: [Colony Sim](https://opengameart.org/content/colony-sim-assets), [Match-3](https://opengameart.org/content/match-3).
SpriteFX: [Plants](https://spritefx.blogspot.com/2013/07/sprite-plants.html), [Lightning](https://spritefx.blogspot.com/2013/04/sprite-lightning.html).
Paweł Pastuszak: [VisUI](https://github.com/kotcrab/vis-ui).

Chris Zabriskie: [Is That You Or Are You You?](https://chriszabriskie.com/reappear), [Divider](https://chriszabriskie.bandcamp.com/album/divider), [CGI Snake](https://chriszabriskie.bandcamp.com/album/divider) ([CC-By-V4](https://creativecommons.org/licenses/by/4.0/))

Steve Matteson: [Open Sans](https://fonts.google.com/specimen/Open+Sans) ([ApacheV2](https://www.apache.org/licenses/LICENSE-2.0)).
Peter Hull: [VT323](https://fonts.google.com/specimen/VT323) ([OFL](http://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL_web)).

tix99: [squeak toy](https://freesound.org/people/tix99/packs/21312/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
FxKid2: [Cute Walk Run C](https://freesound.org/people/FxKid2/sounds/365810/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
Mark DiAngelo: [Blop](https://soundbible.com/2067-Blop.html) ([CC-By-V3](https://creativecommons.org/licenses/by/3.0/)).
man: [swoosh 1](http://soundbible.com/682-Swoosh-1.html) ([CC-SamplingPlus-V1](https://creativecommons.org/licenses/sampling+/1.0/)).
http://soundbible.com/308-Large-Thump-Or-Bump.html
Mike Koenig: [Shooting Star](https://soundbible.com/1744-Shooting-Star.html) ([CC-By-V3](https://creativecommons.org/licenses/by/3.0/)).
Raclure: [Affirmative decision chime](https://freesound.org/people/Raclure/sounds/405547/), [Cancel chime](https://freesound.org/people/Raclure/sounds/405548/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
waveplay_old: [Short Click](https://freesound.org/people/waveplay_old/sounds/399934/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
Planman: [Poof of Smoke](https://freesound.org/people/Planman/sounds/208111/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
MATTIX: [Retro Explosion 5](https://freesound.org/people/MATTIX/sounds/441497/) ([CC-By-V3](https://creativecommons.org/licenses/by/3.0/)).
pel2na: [Two Kazoo Fanfare](https://freesound.org/people/pel2na/sounds/321937/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
Selector: [rocket launch](https://freesound.org/people/Selector/sounds/250200/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
doorajar: [DirtShovel](https://freesound.org/people/doorajar/sounds/427074/) ([CC-By-NC-V3](https://creativecommons.org/licenses/by-nc/3.0/)).
visual: [Industrial Bass 1](https://freesound.org/people/visual/sounds/16156/) ([CC0](https://creativecommons.org/publicdomain/zero/1.0/)).
cameronmusic: [pulse 1](https://freesound.org/people/cameronmusic/sounds/138421/) ([CC-By-V3](https://creativecommons.org/licenses/by/3.0/)).
