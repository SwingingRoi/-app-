from pydub import AudioSegment
from pydub.silence import split_on_silence
import time
import os


def main():
    # 载入
    name = 'C://Users/jhd/Desktop/test.mp3'
    sound = AudioSegment.from_mp3(name)

    # 设置参数
    silence_thresh = -70  # 小于-70dBFS以下的为静默
    min_silence_len = 700  # 静默超过700毫秒则拆分
    length_limit = 60 * 1000  # 拆分后每段不得超过1分钟
    abandon_chunk_len = 500  # 放弃小于500毫秒的段
    joint_silence_len = 1300  # 段拼接时加入1300毫秒间隔用于断句

    # 将录音文件拆分成适合百度语音识别的大小
    path=prepare_for_baiduaip(name, sound, silence_thresh, min_silence_len, length_limit, abandon_chunk_len,
                                 joint_silence_len)
    print(path)


def prepare_for_baiduaip(name, sound, silence_thresh=-70, min_silence_len=700, length_limit=60 * 1000,
                         abandon_chunk_len=500, joint_silence_len=1300):
    '''
    将录音文件拆分成适合百度语音识别的大小
    百度目前免费提供1分钟长度的语音识别。
    先按参数拆分录音，拆出来的每一段都小于1分钟。
    然后将时间过短的相邻段合并，合并后依旧不长于1分钟。

    Args:
        name: 录音文件名
        sound: 录音文件数据
        silence_thresh: 默认-70      # 小于-70dBFS以下的为静默
        min_silence_len: 默认700     # 静默超过700毫秒则拆分
        length_limit: 默认60*1000    # 拆分后每段不得超过1分钟
        abandon_chunk_len: 默认500   # 放弃小于500毫秒的段
        joint_silence_len: 默认1300  # 段拼接时加入1300毫秒间隔用于断句
    Return:
        total：返回拆分个数
    '''

    # 按句子停顿，拆分成长度不大于1分钟录音片段
    chunks = chunk_split_length_limit(sound, min_silence_len=min_silence_len, length_limit=length_limit,
                                      silence_thresh=silence_thresh)  # silence time:700ms and silence_dBFS<-70dBFS

    # 放弃长度小于0.5秒的录音片段
    for i in list(range(len(chunks)))[::-1]:
        if len(chunks[i]) <= abandon_chunk_len:
            chunks.pop(i)

    # 时间过短的相邻段合并，单段不超过1分钟
    chunks = chunk_join_length_limit(chunks, joint_silence_len=joint_silence_len, length_limit=length_limit)

    # 保存前处理一下路径文件名
    pathfold="C://Users/jhd/Desktop/chunks/"
    path=[]
    if not os.path.exists(pathfold): os.mkdir(pathfold)

    # 保存所有分段
    total = len(chunks)
    for i in range(total):
        newpath=pathfold+"chunk"+str(i)+".wav"
        path.append(newpath)
        chunks[i].export('C://Users/jhd/Desktop/chunks/chunk' + str(i) + ".wav", format="wav")

    return path


def chunk_split_length_limit(chunk, min_silence_len=700, length_limit=60 * 1000, silence_thresh=-70, level=0):
    '''
    将声音文件按正常语句停顿拆分，并限定单句最长时间，返回结果为列表形式
    Args:
        chunk: 录音文件
        min_silence_len: 拆分语句时，静默满足该长度，则拆分，默认0.7秒。
        length_limit：拆分后单个文件长度不超过该值，默认1分钟。
        silence_thresh：小于-70dBFS以下的为静默
    Return:
        chunk_splits：拆分后的列表
    '''
    if len(chunk) > length_limit:
        # 长度超过length_limit，拆分
        chunk_splits = split_on_silence(chunk, min_silence_len=min_silence_len, silence_thresh=silence_thresh)
        # 修改静默时长，并检测是否已经触底
        min_silence_len -= 100
        if min_silence_len <= 0:
            tempname = 'C://Users/jhd/Desktop/chunks/temp_%d.wav' % int(time.time())
            chunk.export(tempname, format='wav')
            raise Exception
        # 处理拆分结果
        if len(chunk_splits) < 2:
            # 拆分失败，缩短静默时间后，嵌套chunk_split_length_limit继续拆
            chunk_splits = chunk_split_length_limit(chunk, min_silence_len=min_silence_len, length_limit=length_limit,
                                                    silence_thresh=silence_thresh, level=level + 1)
        else:
            # 拆分成功。
            arr = []
            min_silence_len -= 100
            for c in chunk_splits:
                if len(c) < length_limit:
                    # 长度没length_limit
                    arr.append(c)
                else:
                    # 长度超过length_limit，缩短静默时间后，嵌套chunk_split_length_limit继续拆
                    arr += chunk_split_length_limit(c, min_silence_len=min_silence_len, length_limit=length_limit,
                                                    silence_thresh=silence_thresh, level=level + 1)
            chunk_splits = arr
    else:
        # 长度没超过length_limit，直接返回即可
        chunk_splits = []
        chunk_splits.append(chunk)
    return chunk_splits


def chunk_join_length_limit(chunks, joint_silence_len=1300, length_limit=60 * 1000):
    '''
    将声音文件合并，并限定单句最长时间，返回结果为列表形式
    Args:
        chunk: 录音文件
        joint_silence_len: 合并时文件间隔，默认1.3秒。
        length_limit：合并后单个文件长度不超过该值，默认1分钟。
    Return:
        adjust_chunks：合并后的列表
    '''
    #
    silence = AudioSegment.silent(duration=joint_silence_len)
    adjust_chunks = []
    temp = AudioSegment.empty()
    for chunk in chunks:
        length = len(temp) + len(silence) + len(chunk)  # 预计合并后长度
        if length < length_limit:  # 小于1分钟，可以合并
            temp += silence + chunk
        else:  # 大于1分钟，先将之前的保存，重新开始累加
            adjust_chunks.append(temp)
            temp = chunk
    else:
        adjust_chunks.append(temp)
    return adjust_chunks


if __name__ == '__main__':
    main()


