package com.fontal.fonpicturebackend.model.vo.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PicDatabaseInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 待审数
     */
    private Long reviewingCount;

    /**
     * 通过数
     */
    private Long passCount;
    /**
     * 拒绝数
     */
    private Long rejectCount;

    /**
     * 图总数
     */
    private Long allPicCount;
}
