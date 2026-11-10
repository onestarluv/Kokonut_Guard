/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.gui;

import java.util.Map;
import java.util.HashMap;

final class KoreanFlagNames {
    private static final Map<String, String> NAMES = new HashMap<>();

    static {
        String entries = """
                passthrough=보호 판정 통과
                nonplayer-protection-domains=비플레이어 보호 도메인
                build=건축
                block-break=블록 파괴
                block-place=블록 설치
                use=장치 사용
                interact=상호작용
                damage-animals=동물 공격
                pvp=플레이어 간 전투
                sleep=수면
                respawn-anchors=리스폰 정박기
                tnt=TNT 폭발
                chest-access=보관함 접근
                vehicle-place=탈것 설치
                vehicle-destroy=탈것 파괴
                lighter=불 붙이기
                ride=탑승
                potion-splash=투척용 물약
                item-frame-rotation=아이템 액자 회전
                block-trampling=블록 밟기
                firework-damage=폭죽 피해
                use-anvil=모루 사용
                use-dripleaf=흘림잎 사용
                wind-charge-burst=돌풍구 폭발
                item-pickup=아이템 줍기
                item-drop=아이템 버리기
                exp-drops=경험치 드롭
                mob-damage=몹 공격 피해
                creeper-explosion=크리퍼 폭발
                enderdragon-block-damage=엔더 드래곤 블록 파괴
                ghast-fireball=가스트 화염구
                other-explosion=기타 폭발
                breeze-charge-explosion=브리즈 돌풍 폭발
                wither-damage=위더 피해
                enderman-grief=엔더맨 블록 변경
                snowman-trails=눈 골렘 눈 생성
                ravager-grief=파괴수 블록 파괴
                entity-painting-destroy=엔티티의 그림 파괴
                entity-item-frame-destroy=엔티티의 액자 파괴
                mob-spawning=몹 생성
                deny-spawn=생성 금지 엔티티 목록
                pistons=피스톤
                fire-spread=불 번짐
                lava-fire=용암 발화
                lightning=번개
                ice-form=얼음 생성
                ice-melt=얼음 녹음
                frosted-ice-melt=살얼음 녹음
                frosted-ice-form=살얼음 생성
                snow-fall=눈 쌓임
                snow-melt=눈 녹음
                mushroom-growth=버섯 확산
                leaf-decay=잎 사라짐
                grass-growth=잔디 확산
                mycelium-spread=균사체 확산
                vine-growth=덩굴 성장
                rock-growth=점적석 성장
                sculk-growth=스컬크 확산
                crop-growth=작물 성장
                soil-dry=경작지 건조
                coral-fade=산호 죽음
                copper-fade=구리 산화
                water-flow=물 흐름
                lava-flow=용암 흐름
                moisture-change=경작지 수분 변화
                lava-harden=용암 굳음
                weather-lock=날씨 고정
                time-lock=시간 고정
                send-chat=채팅 보내기
                receive-chat=채팅 받기
                blocked-cmds=금지 명령어 목록
                allowed-cmds=허용 명령어 목록
                teleport=순간이동 위치
                spawn=리스폰 위치
                teleport-message=순간이동 안내
                invincible=무적
                fall-damage=낙하 피해
                natural-health-regen=자연 체력 회복
                natural-hunger-drain=자연 허기 감소
                entry=입장
                exit=퇴장
                exit-override=퇴장 제한 우회
                exit-via-teleport=순간이동으로 퇴장
                enderpearl=엔더 진주
                chorus-fruit-teleport=후렴과 순간이동
                greeting=입장 메시지
                farewell=퇴장 메시지
                greeting-title=입장 타이틀
                farewell-title=퇴장 타이틀
                kokonut-hud=HUD 지역 이름
                notify-enter=입장 알림
                notify-leave=퇴장 알림
                game-mode=게임 모드
                heal-delay=회복 간격
                heal-amount=회복량
                heal-min-health=회복 최소 체력
                heal-max-health=회복 최대 체력
                feed-delay=허기 회복 간격
                feed-amount=허기 회복량
                feed-min-hunger=회복 최소 허기
                feed-max-hunger=회복 최대 허기
                deny-message=행동 거부 메시지
                entry-deny-message=입장 거부 메시지
                exit-deny-message=퇴장 거부 메시지
                """;
        for (String entry : entries.strip().split("\n")) {
            String[] pair = entry.strip().split("=", 2);
            NAMES.put(pair[0], pair[1]);
        }
    }

    private KoreanFlagNames() {
    }

    static String name(String key) {
        return NAMES.getOrDefault(key, key);
    }
}
