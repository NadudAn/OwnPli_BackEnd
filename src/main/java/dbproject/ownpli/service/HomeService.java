package dbproject.ownpli.service;

import dbproject.ownpli.domain.music.MusicEntity;
import dbproject.ownpli.dto.MusicDTO;
import dbproject.ownpli.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class HomeService {
    private final PlaylistMusicRepository playlistMusicRepository;
    private final MusicRepository musicRepository;
    private final MusicService musicService;
    private final MusicLikeRepository musicLikeRepository;
    private final MoodRepository moodRepository;
    private final MusicMoodRepository musicMoodRepository;
    private final UserService userService;
    private final QueryRepository queryRepository;

    /**
     * playlist 많이 담은 순으로 음악 보내기
     * @return
     */
    public List<MusicDTO> findTop10Musics() {
        Optional<List<String>> distinctMusicIdOptional = playlistMusicRepository.findDistinctMusicId();
        List<MusicDTO> musicDTOList = new ArrayList<>();

        if(distinctMusicIdOptional.isEmpty() || distinctMusicIdOptional.get().size() < 10) {
            for (int i = 0; i < 10; i++) {
                musicDTOList.add(musicService.findMusicInfo(musicRepository.findAll().get(i).getMusicId()));
            }
            return musicDTOList;
        }

        List<String> distinctMusicId = distinctMusicIdOptional.get();
        List<MusicList> musicLists = new ArrayList<>();

        for(int i = 0; i < distinctMusicId.size(); i++) {
            musicLists.add(new MusicList(playlistMusicRepository.countByMusicId(distinctMusicId.get(i)), distinctMusicId.get(i)));
        }

        Collections.sort(musicLists, Collections.reverseOrder());


        for (int i = 0; i < 10; i++) {
            musicDTOList.add(musicService.findMusicInfo(musicLists.get(i).musicId));
        }

        return musicDTOList;
    }

    /**
     * 좋아요 많이 받은 순으로 출력
     * @return
     */
    public List<MusicDTO> findTop10LikeList() {
        Optional<List<String>> musicIds = musicLikeRepository.findMusicIds();
        List<MusicDTO> musicDTOList = new ArrayList<>();

        if(musicIds.isEmpty() || musicIds.get().size() < 10) {
            for (int i = 0; i < 10; i++) {
                musicDTOList.add(musicService.findMusicInfo(musicRepository.findAll().get(i).getMusicId()));
            }
            return musicDTOList;
        }

        List<MusicEntity> byMusicId = musicRepository.findByMusicId(musicIds.get());
        List<MusicList> musicLists = new ArrayList<>();

        for(int i = 0; i < byMusicId.size(); i++) {
            musicLists.add(new MusicList(musicLikeRepository.countByMusicId(byMusicId.get(i).getMusicId()).get(), byMusicId.get(i).getMusicId()));
        }

        Collections.sort(musicLists, Collections.reverseOrder());

        for (int i = 0; i < 10; i++) {
            musicDTOList.add(musicService.findMusicInfo(musicLists.get(i).musicId));
        }
        return musicDTOList;
    }

    public List<MusicDTO> ageList(String userId) {
        List<String> ageCompare = queryRepository.findAgeCompare(userService.findByUserId(userId));
        List<String> byPlaylistId = playlistMusicRepository.findByPlaylistId(ageCompare);
        List<MusicDTO> musicInfosByPlaylist = musicService.findMusicInfosByPlaylist(byPlaylistId);

        if(musicInfosByPlaylist.size() < 10 || musicInfosByPlaylist.isEmpty()) {
            List<MusicDTO> musicDTOList = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                musicDTOList.add(musicService.findMusicInfo(musicRepository.findAll().get(i).getMusicId()));
            }
            return musicDTOList;
        }

        return musicInfosByPlaylist;
    }

    public List<MusicDTO> mood5List() {
        Long moodId;
        if(LocalDate.now().getMonthValue() == 12) {
            moodId = moodRepository.findMoodEntityByMood("캐롤");
        }
        else
            moodId = moodRepository.findById((long) ((Math.random() * 10000) % 22)).get().getMoodNum();

        List<String> oneByMoodNum = musicMoodRepository.findOneByMoodNum(moodId);

        return musicService.musicEntitiesToMusicDTO(musicRepository.findByMusicId(oneByMoodNum).subList(0, 6));
    }

    public class MusicList {
        private Long count;
        private String musicId;

        MusicList(Long count, String musicId) {
            this.count = count;
            this.musicId = musicId;
        }
    }
}
