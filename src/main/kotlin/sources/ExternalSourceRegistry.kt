package sources

object ExternalSourceRegistry {

    private val mutableSources: MutableMap<String, IExternalSource<*>> = mutableMapOf()
    val sources: Map<String, IExternalSource<*>>
        get() = mutableSources

    init {
        register("http", HttpCodeSource)
        register("colors", XkcdColorSource)
        register("phrases", GameListSource)
        register("music", MusicSource)
        register("quoter", QuoterSource)
    }

    fun register(name: String, s: IExternalSource<*>) {
        mutableSources[name] = s
    }

    fun unregister(name: String) {
        mutableSources.remove(name)
    }

}