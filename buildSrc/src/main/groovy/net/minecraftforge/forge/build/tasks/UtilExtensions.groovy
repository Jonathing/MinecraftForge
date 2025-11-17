package net.minecraftforge.forge.build.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.jetbrains.annotations.Nullable

import java.security.MessageDigest
import java.text.SimpleDateFormat

@CompileStatic
final class UtilExtensions {
    private UtilExtensions() {}

    @PackageScope static void init() {}

    static {
        initDynamic()
    }

    @CompileDynamic
    private static void initDynamic() {
        File.metaClass.sha1 = { sha1(delegate) }
        File.metaClass.getSha1 = { getSha1(delegate) }
        File.metaClass.sha256 = { sha256(delegate) }
        File.metaClass.getSha256 = { sha256(delegate) }

        File.metaClass.json = { json(delegate) }
        File.metaClass.getJson = { getJson(delegate) }
        File.metaClass.setJson = { json -> setJson(delegate, json) }

        Date.metaClass.iso8601 = { iso8601(delegate) }

        String.metaClass.rsplit = { String del, int limit = -1 -> rsplit(delegate, del, limit) }
    }

    //region Hashing
    static String sha1(File self) {
        var md = MessageDigest.getInstance("SHA-1")
        self.eachByte(4096) { byte[] bytes, int size ->
            md.update(bytes, 0, size)
        }
        md.digest().collect(UtilExtensions.&toHex).join('')
    }

    static @Nullable String getSha1(File self) {
        !self.exists() ? null : sha1(self)
    }

    static String sha256(File self) {
        var md = MessageDigest.getInstance("SHA-256")
        self.eachByte(4096) { byte[] bytes, int size ->
            md.update(bytes, 0, size)
        }
        md.digest().collect(UtilExtensions.&toHex).join('')
    }

    static @Nullable String getSha256(File self) {
        !self.exists() ? null : sha256(self)
    }
    //endregion

    //region JSON
    static Object json(File self) {
        new JsonSlurper().parse(self)
    }

    static def getJson(File self) {
        self.exists() ? json(self) : [:]
    }

    static void setJson(File self, def json) {
        self.text = new JsonBuilder(json).toPrettyString()
    }
    //endregion

    //region Date
    static String iso8601(Date self) {
        var format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        var result = format.format(self)
        return result[0..21] + ':' + result[22..-1]
    }
    //endregion

    //region Strings
    static List<String> rsplit(String self, String del, int limit = -1) {
        var lst = new ArrayList<String>()
        int x = 0
        int idx
        String tmp = self
        while ((idx = tmp.lastIndexOf(del)) != -1 && (limit === -1 || x++ < limit)) {
            lst.add(0, tmp.substring(idx + del.length(), tmp.length()))
            tmp = tmp.substring(0, idx)
        }
        lst.add(0, tmp)
        return lst
    }
    //endregion

    private static String toHex(byte bite) {
        return '%02x'.formatted(bite)
    }
}
