package com.spacewalls.app.data.model

/**
 * One browsable sub-collection. Each entry carries a query per provider so the
 * app asks Wikimedia Commons and the NASA library the phrasing that actually
 * returns good imagery for that subject.
 */
data class SpaceSubcategory(
    val id: String,
    val name: String,
    val wikimediaQuery: String,
    val nasaQuery: String,
    val blurb: String
)

data class SpaceCategoryGroup(
    val id: String,
    val name: String,
    val blurb: String,
    val items: List<SpaceSubcategory>
)

/**
 * The full browsing catalogue. Nothing here is an image — only search intent —
 * so the library stays live and updates whenever the upstream APIs change.
 */
object SpaceCatalog {

    val groups: List<SpaceCategoryGroup> = listOf(
        SpaceCategoryGroup(
            id = "galaxies",
            name = "Galaxies",
            blurb = "Island universes, from our own Milky Way to the deep field.",
            items = listOf(
                SpaceSubcategory("milky-way", "Milky Way", "Milky Way galaxy", "Milky Way galaxy", "Our home galaxy, edge-on and from within."),
                SpaceSubcategory("andromeda", "Andromeda Galaxy", "Andromeda Galaxy", "Andromeda Galaxy", "The nearest large spiral, 2.5 million light years away."),
                SpaceSubcategory("spiral-galaxies", "Spiral Galaxies", "spiral galaxy Hubble", "spiral galaxy", "Dust lanes and arms wound around bright cores."),
                SpaceSubcategory("elliptical-galaxies", "Elliptical Galaxies", "elliptical galaxy", "elliptical galaxy", "Ancient, smooth populations of stars."),
                SpaceSubcategory("distant-galaxies", "Distant Galaxies", "Hubble deep field galaxies", "deep field galaxies", "Light that left before the Sun existed."),
                SpaceSubcategory("galaxy-clusters", "Galaxy Clusters", "galaxy cluster", "galaxy cluster", "Gravitationally bound cities of galaxies.")
            )
        ),
        SpaceCategoryGroup(
            id = "nebulae",
            name = "Nebulae",
            blurb = "Clouds of gas and dust where stars are born and die.",
            items = listOf(
                SpaceSubcategory("orion-nebula", "Orion Nebula", "Orion Nebula", "Orion Nebula", "The brightest stellar nursery in our sky."),
                SpaceSubcategory("eagle-nebula", "Eagle Nebula", "Eagle Nebula", "Eagle Nebula", "Home of the Pillars of Creation."),
                SpaceSubcategory("carina-nebula", "Carina Nebula", "Carina Nebula", "Carina Nebula", "A vast southern complex of glowing gas."),
                SpaceSubcategory("crab-nebula", "Crab Nebula", "Crab Nebula", "Crab Nebula", "The wreckage of a supernova seen in 1054."),
                SpaceSubcategory("horsehead-nebula", "Horsehead Nebula", "Horsehead Nebula", "Horsehead Nebula", "A silhouette of dust against glowing hydrogen."),
                SpaceSubcategory("helix-nebula", "Helix Nebula", "Helix Nebula", "Helix Nebula", "A planetary nebula that looks like an eye."),
                SpaceSubcategory("planetary-nebulae", "Planetary Nebulae", "planetary nebula", "planetary nebula", "The last breath of a Sun-like star.")
            )
        ),
        SpaceCategoryGroup(
            id = "planets",
            name = "Planets",
            blurb = "Worlds of our solar system, imaged up close by real spacecraft.",
            items = listOf(
                SpaceSubcategory("earth", "Earth", "Earth from space Blue Marble", "Earth from space", "The only world we know with a blue sky."),
                SpaceSubcategory("mars", "Mars", "Mars surface rover", "Mars surface", "Rust-red deserts, polar caps and ancient rivers."),
                SpaceSubcategory("jupiter", "Jupiter", "Jupiter Great Red Spot", "Jupiter", "Bands, storms and a moon system of its own."),
                SpaceSubcategory("saturn", "Saturn", "Saturn Cassini rings", "Saturn rings", "Rings of ice a quarter-million kilometres wide."),
                SpaceSubcategory("neptune", "Neptune", "Neptune Voyager 2", "Neptune", "The windiest world in the solar system."),
                SpaceSubcategory("uranus", "Uranus", "Uranus Voyager 2", "Uranus", "A tipped-over ice giant."),
                SpaceSubcategory("venus", "Venus", "Venus planet surface", "Venus planet", "A runaway greenhouse beneath sulphur clouds."),
                SpaceSubcategory("mercury", "Mercury", "Mercury planet MESSENGER", "Mercury planet", "Cratered, airless and Sun-scorched.")
            )
        ),
        SpaceCategoryGroup(
            id = "phenomena",
            name = "Space Phenomena",
            blurb = "The universe in motion — eclipses, flares, aurorae and impacts.",
            items = listOf(
                SpaceSubcategory("black-holes", "Black Holes", "black hole accretion disk", "black hole", "Where gravity wins over light."),
                SpaceSubcategory("solar-eclipses", "Solar Eclipses", "total solar eclipse corona", "solar eclipse", "The Moon exactly covering the Sun."),
                SpaceSubcategory("solar-flares", "Solar Flares", "solar flare", "solar flare", "Magnetic energy released in minutes."),
                SpaceSubcategory("supernovas", "Supernovas", "supernova remnant", "supernova remnant", "Stellar explosions that seed the galaxy."),
                SpaceSubcategory("comets", "Comets", "comet nucleus tail", "comet", "Ice and dust on long, slow orbits."),
                SpaceSubcategory("asteroids", "Asteroids", "asteroid Bennu", "asteroid", "Rocky leftovers of planet formation."),
                SpaceSubcategory("meteor-showers", "Meteor Showers", "meteor shower night sky", "meteor", "Debris burning up in our atmosphere."),
                SpaceSubcategory("northern-lights", "Northern Lights", "aurora borealis", "aurora borealis", "Solar wind lighting up the polar sky.")
            )
        ),
        SpaceCategoryGroup(
            id = "exploration",
            name = "Space Exploration",
            blurb = "The machines and people that carry us outward.",
            items = listOf(
                SpaceSubcategory("astronauts", "Astronauts", "astronaut spacewalk EVA", "astronaut", "People working outside the atmosphere."),
                SpaceSubcategory("iss", "International Space Station", "International Space Station", "International Space Station", "A laboratory in permanent free fall."),
                SpaceSubcategory("space-shuttles", "Space Shuttles", "Space Shuttle orbiter", "Space Shuttle", "Reusable wingships that flew for 30 years."),
                SpaceSubcategory("rockets", "Rockets", "rocket launch", "rocket launch", "Fire, thrust and the edge of the sky."),
                SpaceSubcategory("space-telescopes", "Space Telescopes", "Hubble Space Telescope", "Hubble Space Telescope", "Instruments that look back in time."),
                SpaceSubcategory("moon-missions", "Moon Missions", "Apollo Moon landing", "Apollo Moon", "Six landings, twelve footprints."),
                SpaceSubcategory("mars-rovers", "Mars Rovers", "Mars rover Curiosity", "Mars rover", "Robotic geologists on another world."),
                SpaceSubcategory("deep-space-missions", "Deep Space Missions", "Voyager spacecraft", "Voyager spacecraft", "Probes that have left the heliosphere.")
            )
        ),
        SpaceCategoryGroup(
            id = "collections",
            name = "Special Collections",
            blurb = "Curated moods for the screen you look at most.",
            items = listOf(
                SpaceSubcategory("deep-space", "Deep Space", "deep space stars", "deep space", "Emptiness, stars and distance."),
                SpaceSubcategory("cosmic-darkness", "Cosmic Darkness", "dark nebula silhouette", "dark nebula", "AMOLED-friendly frames of pure night."),
                SpaceSubcategory("blue-universe", "Blue Universe", "blue nebula", "blue nebula", "Cool tones from ice-blue gas clouds."),
                SpaceSubcategory("colorful-nebulae", "Colorful Nebulae", "colorful nebula emission", "colorful nebula", "Hydrogen reds, oxygen teals, dust golds."),
                SpaceSubcategory("planetary-views", "Planetary Views", "planet solar system", "planet", "Worlds framed against black."),
                SpaceSubcategory("space-minimalism", "Space Minimalism", "minimal space star field", "space minimal", "Few elements, a lot of sky.")
            )
        )
    )

    val featured: List<SpaceSubcategory> = listOf(
        groups[1].items[0], groups[0].items[1], groups[1].items[4],
        groups[2].items[3], groups[4].items[1], groups[3].items[7]
    )

    fun subcategory(id: String): SpaceSubcategory? =
        groups.asSequence().flatMap { it.items.asSequence() }.firstOrNull { it.id == id }

    fun group(id: String): SpaceCategoryGroup? = groups.firstOrNull { it.id == id }

    fun all(): List<SpaceSubcategory> = groups.flatMap { it.items }
}
