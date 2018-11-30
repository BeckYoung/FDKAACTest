#include <fdkcodec.h>
#include <aacenc_lib.h>

void releaseInternal();

HANDLE_AACENCODER gAacEncoder;
AACENC_InfoStruct info={0};

JNIEXPORT jint JNICALL
Java_com_example_fdkaactest_fdkaac_FDKCodec_initEncoder(JNIEnv *env, jobject instance, jint sampleRate,
                                     jint channelCount, jint bitRate) {


    if (aacEncOpen(&gAacEncoder, 0x0, channelCount) != AACENC_OK) {
        LOGD("aacEncOpen failed ");
        return -1;
    } else {
        LOGD("aacEncOpen pass ");
    }

    CHANNEL_MODE mode;
    switch (channelCount) {
        case 1:
            mode = MODE_1;
            break;
        case 2:
            mode = MODE_2;
            break;
        case 3:
            mode = MODE_1_2;
            break;
        case 4:
            mode = MODE_1_2_1;
            break;
        case 5:
            mode = MODE_1_2_2;
            break;
        case 6:
            mode = MODE_1_2_2_1;
            break;
        default:
            mode = MODE_1;
            break;
    }
    LOGD("AACENC_CHANNELMODE %d", mode);

    aacEncoder_SetParam(gAacEncoder, AACENC_AOT, AOT_AAC_LC);
    aacEncoder_SetParam(gAacEncoder, AACENC_CHANNELMODE, mode);
    aacEncoder_SetParam(gAacEncoder, AACENC_SAMPLERATE, sampleRate);
    aacEncoder_SetParam(gAacEncoder, AACENC_BITRATE, bitRate);
    aacEncoder_SetParam(gAacEncoder, AACENC_BITRATEMODE, 0);
    aacEncoder_SetParam(gAacEncoder, AACENC_TRANSMUX, TT_MP4_ADTS);
    aacEncoder_SetParam(gAacEncoder, AACENC_PROTECTION, 1);
//    aacEncoder_SetParam(gAacEncoder, AACENC_AFTERBURNER, 1);//可选


    if (aacEncEncode(gAacEncoder, NULL, NULL, NULL, NULL) != AACENC_OK) {
        LOGD("Init encoder failed");
        return -1;
    } else {
        LOGD("Init encoder pass");
    }

    //AACENC_InfoStruct info = {0};

    LOGD("AACENC_InfoStruct  %p", info);

    if (aacEncInfo(gAacEncoder, &info) != AACENC_OK) {
        LOGD("aacEncInfo failed ");
        return -1;
    } else {
        LOGD("aacEncInfo pass ");
        LOGD("frameLength: %d ,inputChannels: %d, maxOutBufBytes: %d", info.frameLength,
             info.inputChannels,
             info.maxOutBufBytes);
        return info.frameLength;
    }


//    LIB_INFO lib_info;
//    if (aacEncGetLibInfo(&lib_info) != AACENC_OK) {
//        LOGD("aacEncGetLibInfo failed ");
//    } else {
//        LOGD("aacEncGetLibInfo pass ");
//    }
//
//    LOGD("title: %s  versionStr: %s", lib_info.title, lib_info.versionStr);

}

JNIEXPORT jbyteArray JNICALL
Java_com_example_fdkaactest_fdkaac_FDKCodec_encode(JNIEnv *env, jobject instance, jshortArray input_,jint len) {

    jshort *input = NULL;
    int input_size=info.frameLength*2*info.inputChannels;
    //int sizeInBuffer =len;
    if (NULL != input_) {
        input = env->GetShortArrayElements(input_, NULL);
        //sizeInBuffer = env->GetArrayLength(input_);
    }

    AACENC_BufDesc inBufDesc = {0};
    AACENC_BufDesc outBufDesc = {0};

    AACENC_InArgs inArgs = {0};
    AACENC_OutArgs outArgs = {0};

    int inIdentifier = IN_AUDIO_DATA;
    int inElSizes = 2;
    void *in_ptr = input;

    if (len <= 0) {
        inArgs.numInSamples = -1;
    } else {
        inArgs.numInSamples = len;

        inBufDesc.numBufs = 1;
        inBufDesc.bufferIdentifiers = &inIdentifier;
        inBufDesc.bufs = &in_ptr;
        inBufDesc.bufSizes = &input_size;
        inBufDesc.bufElSizes = &inElSizes;
    }

    int outBufferSize = info.maxOutBufBytes;
    LOGD("before encode --> numInSamples %d   bufSizes %d outBufferSize %d", inArgs.numInSamples,
         input_size,outBufferSize);
    int8_t outBuffer[outBufferSize];

    int outIdentifier = OUT_BITSTREAM_DATA;
    int bufElSizes = 1;
    void *out_ptr = outBuffer;

    outBufDesc.numBufs = 1;
    outBufDesc.bufs = &out_ptr;
    outBufDesc.bufferIdentifiers = &outIdentifier;
    outBufDesc.bufSizes = &outBufferSize;
    outBufDesc.bufElSizes = &bufElSizes;

    int code = aacEncEncode(gAacEncoder, &inBufDesc, &outBufDesc, &inArgs, &outArgs);

    if (code == AACENC_ENCODE_EOF) {
        LOGD("Encode finished,  flush");
    } else if (code == AACENC_OK) {
        LOGD("Encode is in processing");
    } else {
        LOGD("Error happened code: %d", code);
    }

    LOGD("after encode --> numOutBytes %d   numInSamples %d", outArgs.numOutBytes,
         outArgs.numInSamples);

    jbyteArray outArray = NULL;
    if (outArgs.numOutBytes != 0) {
        outArray = env->NewByteArray(outArgs.numOutBytes);
        env->SetByteArrayRegion(outArray, 0, outArgs.numOutBytes, outBuffer);
    }

    if (NULL != input_) {
        env->ReleaseShortArrayElements(input_, input, 0);
    }

    return outArray;
}


JNIEXPORT void JNICALL
Java_com_example_fdkaactest_fdkaac_FDKCodec_releaseEncoder(JNIEnv *env, jobject instance) {

    if (NULL != gAacEncoder) {
        AACENC_ERROR closeCode = aacEncClose(&gAacEncoder);
        if (closeCode != AACENC_OK) {
            LOGD("aacEncClose failed ");
        } else {
            LOGD("aacEncClose pass ");
        }
    }

}

HANDLE_AACDECODER gAacDecoder;
int16_t *decodeBuffer;
uint8_t *outBuffer;
int outBufferSize = 2 * 8 * 2048;

JNIEXPORT void JNICALL
Java_com_example_fdkaactest_fdkaac_FDKCodec_decode(JNIEnv *env, jobject instance) {

    gAacDecoder = aacDecoder_Open(TT_MP4_ADTS, 1);

}


JNIEXPORT jbyteArray JNICALL
Java_com_example_fdkaactest_fdkaac_FDKCodec_decode(JNIEnv *env, jobject instance, jbyteArray input_) {
    jbyte *src = env->GetByteArrayElements(input_, NULL);
    jint size = env->GetArrayLength(input_);

    uint8_t *input = (uint8_t *) src;

    if (size < 7) {
        LOGD("Invalid adts header's length");
        env->ReleaseByteArrayElements(input_, src, 0);
        return NULL;
    }

    if (input[0] != 0xff || (input[1] & 0xf0) != 0xf0) {
        LOGD("Not an adts packet");
        env->ReleaseByteArrayElements(input_, src, 0);
        return NULL;
    }

    int packetLength = ((input[3] & 0x03) << 11) | (input[4] << 3) | (input[5] >> 5);
    LOGD("Packet length: %d", packetLength);
    if (packetLength != size) {
        LOGD("Partial packet");
        env->ReleaseByteArrayElements(input_, src, 0);
        return NULL;
    }

    uint validLength = packetLength;

    AAC_DECODER_ERROR code = aacDecoder_Fill(gAacDecoder, &input, (const UINT *) &packetLength,
                                             &validLength);

    if (code != AAC_DEC_OK) {
        LOGD("Fill buffer failed");
        env->ReleaseByteArrayElements(input_, src, 0);
        return NULL;
    }

    decodeBuffer = new int16_t[outBufferSize];
    outBuffer = new uint8_t[outBufferSize];

    code = aacDecoder_DecodeFrame(gAacDecoder, decodeBuffer, outBufferSize / sizeof(INT_PCM), 0);
    if (code == AAC_DEC_NOT_ENOUGH_BITS) {
        LOGD("Aac decode not enough bits");
    } else if (code != AAC_DEC_OK) {
        LOGD("Decode failed");
    }

    jbyteArray target = NULL;
    int frame_size = 0;

    if (code == AAC_DEC_OK) {
        CStreamInfo *info = aacDecoder_GetStreamInfo(gAacDecoder);

        if (!info || info->sampleRate <= 0) {
            LOGD("No stream info");
            releaseInternal();
            return NULL;
        }

        LOGD("Stream info frameSize: %d, numChannels: %d ", info->frameSize, info->numChannels);
        frame_size = info->frameSize * info->numChannels;

        for (int i = 0; i < frame_size; i++) {
            uint8_t *out = &outBuffer[2 * i];
            out[0] = decodeBuffer[i] & 0xff;
            out[1] = decodeBuffer[i] >> 8;
        }

        if (frame_size != 0) {
            target = env->NewByteArray(frame_size * 2);
            env->SetByteArrayRegion(target, 0, frame_size * 2, (const jbyte *) outBuffer);
        }
    }

    env->ReleaseByteArrayElements(input_, src, 0);
    return target;
}

void releaseInternal() {
    if (outBuffer) {
        delete (outBuffer);
    }

    if (decodeBuffer) {
        delete (decodeBuffer);
    }

    LOGD("Delete any allocated memory ");
}

JNIEXPORT void JNICALL
Java_com_example_fdkaactest_fdkaac_FDKCodec_releaseDecoder(JNIEnv *env, jobject instance) {
    LOGD("aacDecoder_Close ");
    if (gAacDecoder) {
        aacDecoder_Close(gAacDecoder);
    }

    if (outBuffer) {
        delete (outBuffer);
    }

    if (decodeBuffer) {
        delete (decodeBuffer);
    }

}