# Kokonut_Guard

플러그인 ID는 `Kokonut_Guard`, 배포 JAR는 `Kokonut_Guard-<version>.jar`입니다. 작성자·설명·사이트는 Core와 같은 `pingpong`·`에고버스 플러그인`·`https://wiki.egoverse.kr`로 통일했습니다. `provides: [WorldGuard]`로 기존 의존성과 플러그인 조회 이름을 지원하며 WorldGuard API 패키지·권한은 유지합니다.

첫 로드 시 `plugins/WorldGuard`의 영역·설정을 `plugins/Kokonut_Guard`로 이전합니다. 양쪽 폴더가 모두 자료를 포함하면 덮어쓰지 않고 로드를 실패시킵니다. 교체 시 기존 `worldguard-bukkit-*-dist.jar`와 새 JAR를 함께 설치하지 마세요. 소스·라이선스는 아래 원본 설명을 따릅니다.

<h1>
    <img src="worldguard-logo.svg" alt="WorldGuard" width="400" /> 
</h1>

WorldGuard lets you and players guard areas of land against griefers and undesirables, as well as tweak and disable various gameplay features of Minecraft.

* Block creeper and wither block damage, falling damage, etc.
* Disable fire spread, lava fire spread, ice formation, Endermen picking up blocks, etc.
* Blacklist certain items and blocks so they can't be used
* Warn moderators when certain items and blocks are used
* Protect areas of your world so only certain people can build in them
* Set areas where PVP, TNT, mob damage, and other features are disabled
* Protect your server from various 'exploits' like magical obsidian creation machines
* Disable, or enable, various Minecraft features, like sponges from classic
* Add useful commands like an immediate "STOP ALL FIRE SPREAD" command
* Enable only features you want! Everything is off by default

WorldGuard is open source and is available under the GNU Lesser
General Public License v3.

A Bukkit server implementation (such as [Paper](https://papermc.io)) and the [WorldEdit plugin](https://dev.bukkit.org/projects/worldedit) are required to use WorldGuard. You can get a release copy of WorldGuard from the [BukkitDev site](https://dev.bukkit.org/projects/worldguard).

Compiling
---------

See [COMPILING.md](COMPILING.md).

Contributing
------------

We happily accept contributions, especially through pull requests on GitHub.

Please read CONTRIBUTING.md for important guidelines to follow.

Submissions must be licensed under the GNU Lesser General Public License v3.

Links
-----

* [Homepage](https://enginehub.org/worldguard)
* [Discord](https://discord.gg/enginehub)
* [Issue tracker](https://github.com/EngineHub/WorldGuard/issues)
* [Continuous integration](https://builds.enginehub.org) [![Build Status](https://ci.enginehub.org/app/rest/builds/buildType:bt11,branch:master/statusIcon.svg)](http://ci.enginehub.org/viewType.html?buildTypeId=bt11&guest=1)
* [End-user documentation](https://worldguard.enginehub.org/en/latest/)
