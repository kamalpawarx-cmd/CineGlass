package com.example.data.seed

import com.example.data.local.AddonRepositoryEntity
import com.example.data.local.MediaItemEntity
import com.example.data.model.EpisodeItem
import com.example.data.model.LanguageTrack
import com.example.data.model.MediaCategory
import com.example.data.model.SupportedLanguages
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object OpenCinemaCatalog {
    const val REPO_BLENDER = "repo_open_cinema"
    const val REPO_SERIES_HUB = "repo_open_series"
    const val REPO_ANIME_NEXUS = "repo_anime_nexus"
    const val REPO_ADAPTIVE_HLS = "repo_hls_ultra"
    const val REPO_USER_UPLOADS = "repo_user_uploads"
    const val REPO_CUSTOM_LINKS = "repo_custom_links"

    fun starterRepositories(): List<AddonRepositoryEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            AddonRepositoryEntity(
                id = REPO_BLENDER,
                name = "Open Cinema Vault",
                description = "High-bitrate Creative Commons 4K & 1080p sci-fi, fantasy, and indie feature films with multi-language tracks.",
                manifestUrl = "https://studio.blender.org/films/catalog.json",
                siteUrl = "https://studio.blender.org/films/",
                author = "Blender Open Movies",
                iconBadge = "4K",
                categoriesCsv = "MOVIES,ANIME",
                isEnabled = true,
                isBuiltIn = true,
                itemCount = 6,
                lastSyncedAt = now
            ),
            AddonRepositoryEntity(
                id = REPO_ADAPTIVE_HLS,
                name = "UltraHLS Adaptive Mirror",
                description = "Multi-bitrate HLS (.m3u8) adaptive streams engineered for instant scrubbing and quality switching from 480p to 4K.",
                manifestUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                siteUrl = "https://mux.com",
                author = "Adaptive Media Mesh",
                iconBadge = "HLS",
                categoriesCsv = "MOVIES,WEB_SERIES",
                isEnabled = true,
                isBuiltIn = true,
                itemCount = 3,
                lastSyncedAt = now - 120_000L
            ),
            AddonRepositoryEntity(
                id = REPO_SERIES_HUB,
                name = "Horizon Web Series Feed",
                description = "Curated multi-episode web series, thrillers, and documentary anthologies ready for binge streaming & offline download.",
                manifestUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/",
                siteUrl = "https://archive.org/details/movies",
                author = "Open Series Collective",
                iconBadge = "TV",
                categoriesCsv = "WEB_SERIES",
                isEnabled = true,
                isBuiltIn = true,
                itemCount = 5,
                lastSyncedAt = now - 240_000L
            ),
            AddonRepositoryEntity(
                id = REPO_ANIME_NEXUS,
                name = "Aether Anime & Animation",
                description = "Sub & Dub animated worlds featuring Japanese, English, Hindi, and Spanish audio/subtitle profiles.",
                manifestUrl = "https://studio.blender.org/films/anime-feed.json",
                siteUrl = "https://studio.blender.org/",
                author = "Aether Open Sub/Dub",
                iconBadge = "SUB",
                categoriesCsv = "ANIME",
                isEnabled = true,
                isBuiltIn = true,
                itemCount = 5,
                lastSyncedAt = now - 360_000L
            )
        )
    }

    fun starterMedia(moshi: Moshi): List<MediaItemEntity> {
        val epType = Types.newParameterizedType(List::class.java, EpisodeItem::class.java)
        val langType = Types.newParameterizedType(List::class.java, LanguageTrack::class.java)
        val epAdapter = moshi.adapter<List<EpisodeItem>>(epType)
        val langAdapter = moshi.adapter<List<LanguageTrack>>(langType)

        fun langs(vararg names: String): String = langAdapter.toJson(SupportedLanguages.fromNames(names.toList()))
        fun eps(list: List<EpisodeItem>): String = epAdapter.toJson(list)

        val baseTime = System.currentTimeMillis()

        return listOf(
            // ==================== MOVIES ====================
            MediaItemEntity(
                id = "mov_tears_of_steel",
                title = "Tears of Steel",
                category = MediaCategory.MOVIES.key,
                synopsis = "In an apocalyptic Amsterdam ruled by rogue cyborgs, a band of resistance engineers fights to rewrite a crucial memory before the city falls.",
                year = 2024,
                rating = 8.9,
                durationLabel = "12m • 4K HDR",
                genresCsv = "Sci-Fi,Cyberpunk,Action",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
                localPosterKey = "movies_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                hlsUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                maxQuality = "4K",
                languagesJson = langs("English", "Hindi", "Spanish", "French", "German", "Japanese"),
                episodesJson = "[]",
                repoId = REPO_BLENDER,
                repoName = "Open Cinema Vault",
                createdAt = baseTime
            ),
            MediaItemEntity(
                id = "mov_sintel",
                title = "Sintel: Dragon's Oath",
                category = MediaCategory.MOVIES.key,
                synopsis = "A lone wanderer crosses frozen tundras and ancient desert citadels in search of a baby dragon she once rescued, uncovering a devastating secret.",
                year = 2025,
                rating = 9.1,
                durationLabel = "15m • 4K HDR",
                genresCsv = "Fantasy,Adventure,Drama",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                localPosterKey = "movies_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                hlsUrl = "",
                maxQuality = "4K",
                languagesJson = langs("English", "Hindi", "Japanese", "Spanish", "Korean"),
                episodesJson = "[]",
                repoId = REPO_BLENDER,
                repoName = "Open Cinema Vault",
                createdAt = baseTime - 10_000L
            ),
            MediaItemEntity(
                id = "mov_big_buck_hls",
                title = "Big Buck Bunny: Adaptive UHD",
                category = MediaCategory.MOVIES.key,
                synopsis = "A gentle giant of the forest wakes up to spring only to face three mischievous woodland pranksters in this iconic adaptive HLS showcase.",
                year = 2024,
                rating = 8.4,
                durationLabel = "10m • HLS 4K",
                genresCsv = "Comedy,Family,Adventure",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                localPosterKey = "movies_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                maxQuality = "4K",
                languagesJson = langs("English", "Hindi", "Spanish", "French", "Arabic"),
                episodesJson = "[]",
                repoId = REPO_ADAPTIVE_HLS,
                repoName = "UltraHLS Adaptive Mirror",
                createdAt = baseTime - 20_000L
            ),
            MediaItemEntity(
                id = "mov_elephants_dream",
                title = "Elephants Dream: Machine Mind",
                category = MediaCategory.MOVIES.key,
                synopsis = "Two travelers venture deep inside a surreal mechanical megastructure where every gear reflects the architecture of their own minds.",
                year = 2023,
                rating = 8.3,
                durationLabel = "11m • 1080p",
                genresCsv = "Sci-Fi,Mystery,Surreal",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                localPosterKey = "movies_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("English", "German", "French", "Hindi", "Japanese"),
                episodesJson = "[]",
                repoId = REPO_BLENDER,
                repoName = "Open Cinema Vault",
                createdAt = baseTime - 30_000L
            ),
            MediaItemEntity(
                id = "mov_bullrun",
                title = "Apex Bullrun: Midnight Drive",
                category = MediaCategory.MOVIES.key,
                synopsis = "High-octane coastal rally cinema following precision drivers tearing through twilight canyon switchbacks.",
                year = 2025,
                rating = 8.1,
                durationLabel = "9m • 1080p",
                genresCsv = "Action,Racing,Thriller",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/WeAreGoingOnBullrun.jpg",
                localPosterKey = "movies_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/WeAreGoingOnBullrun.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("English", "Hindi", "Spanish"),
                episodesJson = "[]",
                repoId = REPO_ADAPTIVE_HLS,
                repoName = "UltraHLS Adaptive Mirror",
                createdAt = baseTime - 40_000L
            ),
            MediaItemEntity(
                id = "mov_blaze_protocol",
                title = "Blaze Protocol",
                category = MediaCategory.MOVIES.key,
                synopsis = "An explosive tactical short filmed with razor-sharp contrast and kinetic pacing across neon-drenched rooftops.",
                year = 2025,
                rating = 8.5,
                durationLabel = "1080p • HDR",
                genresCsv = "Action,Thriller,Sci-Fi",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
                localPosterKey = "movies_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("English", "Hindi", "Korean", "Arabic"),
                episodesJson = "[]",
                repoId = REPO_BLENDER,
                repoName = "Open Cinema Vault",
                createdAt = baseTime - 50_000L
            ),

            // ==================== WEB SERIES ====================
            MediaItemEntity(
                id = "ser_lighthouse_chronicles",
                title = "The Glass Beacon",
                category = MediaCategory.WEB_SERIES.key,
                synopsis = "When an automated coastal lighthouse starts transmitting encrypted future coordinates, a marine investigator uncovers a century-old maritime conspiracy.",
                year = 2026,
                rating = 9.2,
                durationLabel = "S1 • 4 Episodes",
                genresCsv = "Thriller,Mystery,Sci-Fi",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg",
                localPosterKey = "series_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                hlsUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                maxQuality = "4K",
                languagesJson = langs("English", "Hindi", "Spanish", "French", "German"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Signal in the Fog", "12m", "An anomalous sonar beacon wakes the coastal station at midnight.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
                        EpisodeItem(2, 1, "The Escape Vector", "10m", "A submerged relay vault reveals blueprints from twenty years ahead.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
                        EpisodeItem(3, 1, "Salt & Circuit", "11m", "Power cuts across the archipelago while an unknown vessel docks.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                        EpisodeItem(4, 1, "Zero Horizon", "14m", "The beacon reaches full resonance before dawn.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4")
                    )
                ),
                repoId = REPO_SERIES_HUB,
                repoName = "Horizon Web Series Feed",
                createdAt = baseTime - 5_000L
            ),
            MediaItemEntity(
                id = "ser_neon_overdrive",
                title = "Neon Overdrive",
                category = MediaCategory.WEB_SERIES.key,
                synopsis = "Underground street engineers build neural-assisted hypercars to outrun corporate trackers across Neo-Pacific highways.",
                year = 2025,
                rating = 8.7,
                durationLabel = "S1 • 3 Episodes",
                genresCsv = "Action,Crime,Cyberpunk",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerJoyrides.jpg",
                localPosterKey = "series_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerJoyrides.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("English", "Hindi", "Korean", "Spanish"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Ignition Sequence", "9m", "The midnight circuit opens with a stolen telemetry core.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"),
                        EpisodeItem(2, 1, "Redline Drift", "10m", "A high-speed pursuit through rain-soaked canyons.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"),
                        EpisodeItem(3, 1, "Apex Finale", "10m", "All rival crews converge on the coastal viaduct.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4")
                    )
                ),
                repoId = REPO_SERIES_HUB,
                repoName = "Horizon Web Series Feed",
                createdAt = baseTime - 15_000L
            ),
            MediaItemEntity(
                id = "ser_offgrid_chronicle",
                title = "Off-Grid Frontiers",
                category = MediaCategory.WEB_SERIES.key,
                synopsis = "An unscripted automotive and wilderness docu-series pushing modern machines across volcanic dust trails and alpine ridges.",
                year = 2025,
                rating = 8.5,
                durationLabel = "S2 • 3 Episodes",
                genresCsv = "Documentary,Adventure",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/SubaruOutbackOnStreetAndDirt.jpg",
                localPosterKey = "series_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/SubaruOutbackOnStreetAndDirt.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("English", "German", "French", "Hindi"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Street & Dirt", "10m", "Testing suspension limits across desert mesas.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"),
                        EpisodeItem(2, 1, "Track Precision", "10m", "Inside the engineering cockpit of hot-hatch tuning.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4"),
                        EpisodeItem(3, 1, "Thermal Meltdown", "8m", "Extreme altitude endurance trials.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4")
                    )
                ),
                repoId = REPO_SERIES_HUB,
                repoName = "Horizon Web Series Feed",
                createdAt = baseTime - 25_000L
            ),
            MediaItemEntity(
                id = "ser_quantum_breach",
                title = "Quantum Breach",
                category = MediaCategory.WEB_SERIES.key,
                synopsis = "Inside an orbital research ring, two physicists realize their simulation is leaking into real-world timelines.",
                year = 2026,
                rating = 8.8,
                durationLabel = "S1 • 2 Episodes",
                genresCsv = "Sci-Fi,Drama,Mystery",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerMeltdowns.jpg",
                localPosterKey = "series_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerMeltdowns.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                hlsUrl = "",
                maxQuality = "4K",
                languagesJson = langs("English", "Hindi", "Japanese", "Arabic"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Event Horizon", "11m", "Containment fields flicker as anomalous telemetry arrives.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"),
                        EpisodeItem(2, 1, "Mirror State", "12m", "The crew races to synchronize both timelines.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
                    )
                ),
                repoId = REPO_ADAPTIVE_HLS,
                repoName = "UltraHLS Adaptive Mirror",
                createdAt = baseTime - 35_000L
            ),

            // ==================== ANIME ====================
            MediaItemEntity(
                id = "ani_sky_citadel",
                title = "Aether Citadel: Crimson Wing",
                category = MediaCategory.ANIME.key,
                synopsis = "Above a sea of luminescent clouds, a young sky-forger awakens a dormant crystal dragon to defend the floating archipelago from eclipse raiders.",
                year = 2026,
                rating = 9.4,
                durationLabel = "15m • Sub/Dub 4K",
                genresCsv = "Fantasy,Shonen,Adventure",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                localPosterKey = "anime_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                maxQuality = "4K",
                languagesJson = langs("Japanese", "English", "Hindi", "Spanish", "Korean"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Episode 1: Crystal Dawn", "15m", "The relic beneath the cloud spire reacts to an ancient melody.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
                        EpisodeItem(2, 1, "Episode 2: Clockwork Spire", "11m", "Crossing the mechanical labyrinth guarding the northern gate.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                        EpisodeItem(3, 1, "Episode 3: Meadow Guardian", "10m", "An unexpected ally joins the guild before the eclipse.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                    )
                ),
                repoId = REPO_ANIME_NEXUS,
                repoName = "Aether Anime & Animation",
                createdAt = baseTime - 2_000L
            ),
            MediaItemEntity(
                id = "ani_clockwork_soul",
                title = "Clockwork Soul: Labyrinth 09",
                category = MediaCategory.ANIME.key,
                synopsis = "In a surreal cyber-fantasy tower, an apprentice navigator questions whether his mentor's vision of the machine world is salvation or illusion.",
                year = 2025,
                rating = 8.9,
                durationLabel = "S1 • 3 Episodes",
                genresCsv = "Mecha,Sci-Fi,Psychological",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                localPosterKey = "anime_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("Japanese", "English", "Hindi", "French"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Episode 1: The Cable Hall", "11m", "Entering Sector 09 where living wires rewrite gravity.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                        EpisodeItem(2, 1, "Episode 2: Steel Memory", "12m", "Fragments of an older world emerge through the visor.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
                        EpisodeItem(3, 1, "Episode 3: Awakening", "10m", "Breaking free from the phantom core.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                    )
                ),
                repoId = REPO_ANIME_NEXUS,
                repoName = "Aether Anime & Animation",
                createdAt = baseTime - 12_000L
            ),
            MediaItemEntity(
                id = "ani_forest_spirit",
                title = "Mori no Kaze: Forest Spirit",
                category = MediaCategory.ANIME.key,
                synopsis = "A heartwarming animated slice-of-life adventure set in a sun-drenched spirit glade full of quirky forest guardians.",
                year = 2024,
                rating = 8.6,
                durationLabel = "10m • Sub/Dub HD",
                genresCsv = "Slice of Life,Comedy,Fantasy",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                localPosterKey = "anime_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                maxQuality = "4K",
                languagesJson = langs("Japanese", "English", "Hindi", "Spanish", "Korean"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Episode 1: Morning Breeze", "10m", "A peaceful day turns chaotic when three woodland spirits strike.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                        EpisodeItem(2, 1, "Episode 2: Festival Spark", "8m", "Lanterns light the valley for the summer equinox.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
                    )
                ),
                repoId = REPO_ANIME_NEXUS,
                repoName = "Aether Anime & Animation",
                createdAt = baseTime - 22_000L
            ),
            MediaItemEntity(
                id = "ani_solar_flare",
                title = "Solar Flare Academy",
                category = MediaCategory.ANIME.key,
                synopsis = "Cadets trained to pilot prism-powered gliders compete in the annual orbital tournament across neon skyways.",
                year = 2026,
                rating = 8.8,
                durationLabel = "S1 • 2 Episodes",
                genresCsv = "Action,Shonen,Sports",
                posterUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg",
                localPosterKey = "anime_hero",
                backdropUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                hlsUrl = "",
                maxQuality = "1080p",
                languagesJson = langs("Japanese", "English", "Hindi", "Arabic"),
                episodesJson = eps(
                    listOf(
                        EpisodeItem(1, 1, "Episode 1: Prism Launch", "8m", "First flight trials over the glass arena.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"),
                        EpisodeItem(2, 1, "Episode 2: Dragon Circuit", "14m", "Rivals collide at the final altitude gate.", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4")
                    )
                ),
                repoId = REPO_ANIME_NEXUS,
                repoName = "Aether Anime & Animation",
                createdAt = baseTime - 32_000L
            )
        )
    }

    val sampleRepoJsonTemplate: String = """
{
  "name": "Community Open Cinema Pack",
  "description": "Custom JSON catalog with HD movies, web series, and anime streams.",
  "author": "Community Curator",
  "siteUrl": "https://studio.blender.org/films/",
  "iconBadge": "JSON",
  "items": [
    {
      "title": "Tears of Steel (Director Cut)",
      "category": "MOVIES",
      "synopsis": "Sci-fi cybernetic epic streamed directly from your custom JSON repository.",
      "year": 2026,
      "rating": 9.0,
      "duration": "12m • 4K",
      "genres": ["Sci-Fi", "Action"],
      "posterUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
      "streamUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
      "maxQuality": "4K",
      "languages": ["English", "Hindi", "Japanese", "Spanish"]
    },
    {
      "title": "Sintel Chronicles (Sub/Dub)",
      "category": "ANIME",
      "synopsis": "Dragon quest anime feature linked via custom repository manifest.",
      "year": 2026,
      "rating": 9.2,
      "duration": "15m • 4K",
      "genres": ["Fantasy", "Anime"],
      "posterUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
      "streamUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
      "maxQuality": "4K",
      "languages": ["Japanese", "English", "Hindi"]
    }
  ]
}
    """.trimIndent()
}
