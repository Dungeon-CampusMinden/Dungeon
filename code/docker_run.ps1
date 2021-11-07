docker run --rm -it -p 8080:8080 -u "${id -u}:${id -g}" -v "${pwd}:/code" -w "/code" gradle:7.2.0-jdk11 gradle html:superDev
