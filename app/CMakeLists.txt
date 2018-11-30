# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(FDKAAC_path ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs)


add_library(
             fdkaac-lib
             SHARED
             IMPORTED )

set_target_properties(
                        fdkaac-lib
                        PROPERTIES
                        IMPORTED_LOCATION
                        ${FDKAAC_path}/${ANDROID_ABI}/libfdk-aac.so )



add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/fdkcodec.cpp )
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(src/main/cpp/include src/main/cpp)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )


target_link_libraries( # Specifies the target library.
                       native-lib fdkaac-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )