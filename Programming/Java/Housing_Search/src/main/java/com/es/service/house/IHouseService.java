package com.es.service.house;

import com.es.base.HouseSubscribeStatus;
import com.es.service.ServiceMultiResult;
import com.es.service.ServiceResult;
import com.es.web.dto.HouseDTO;
import com.es.web.dto.HouseSubscribeDTO;
import com.es.web.form.DatatableSearch;
import com.es.web.form.HouseForm;
import com.es.web.form.RentSearch;
import org.springframework.data.util.Pair;

import java.util.Date;

/**
 * <h1>房屋管理接口</h1>
 * Created by LiuYang on 2018/10/4 3:19 PM
 */
public interface IHouseService {

    /** 新增房源 */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    /** 更新 */
    ServiceResult update(HouseForm houseForm);

    /** 查询，附带分页 */
    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBogy);

    /** 查询完整房源信息 */
    ServiceResult<HouseDTO> findCompleteOne(Long id);

    /** 移除图片 */
    ServiceResult removePhoto(Long id);

    /** 更新封面 */
    ServiceResult updateCover(Long coverId, Long targetId);

    /** 新增标签 */
    ServiceResult addTag(Long houseId, String tag);

    /** 移除标签 */
    ServiceResult removeTag(Long houseId, String tag);

    /** 更新房源状态 */
    ServiceResult updateStatus(Long id, int value);

    /** 查询房源信息集 */
    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);

    /** 加入预约清单 */
    ServiceResult addSubscribeOrder(Long houseId);

    /** 获取对应状态的预约列表 */
    ServiceMultiResult<Pair<HouseDTO,HouseSubscribeDTO>> querySubscribeList(
            HouseSubscribeStatus status, int start, int size);

    /** 预约看房时间 */
    ServiceResult subscribe(Long houseId, Date orderTime, String telephone, String desc);

    /** 取消预约 */
    ServiceResult cancelSubscribe(Long houseId);

    /** 管理员查询预约接口 */
    ServiceMultiResult<Pair<HouseDTO,HouseSubscribeDTO>> findSubscribeList(int start, int size);

    /** 完成预约 */
    ServiceResult finishSubscribe(Long houseId);
}
