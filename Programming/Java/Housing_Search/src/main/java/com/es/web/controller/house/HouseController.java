package com.es.web.controller.house;

import com.es.base.ApiResponse;
import com.es.base.RentValueBlock;
import com.es.entity.SupportAddress;
import com.es.service.ServiceMultiResult;
import com.es.service.ServiceResult;
import com.es.service.house.IAddressService;
import com.es.service.house.IHouseService;
import com.es.service.search.ISearchService;
import com.es.service.user.IUserService;
import com.es.web.dto.*;
import com.es.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * <h1>添加房源页面控制器</h1>
 * Created by LiuYang on 2018/10/3 10:53 PM
 */
@Controller
public class HouseController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISearchService searchService;

    /**
     * 自动补全接口
     */
    @GetMapping("rent/house/autocomplete")
    @ResponseBody
    public ApiResponse autocomplete(@RequestParam(value = "prefix") String prefix) {

        if (prefix.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        ServiceResult<List<String>> result = this.searchService.suggest(prefix);
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取支持城市列表
     */
    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities() {
        final ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
        if (result.getTotal() < 1) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应城市支持区域列表
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        final ServiceMultiResult<SupportAddressDTO> addressResult = addressService
                .findAllRegionsByCityName(cityEnName);
        if (addressResult.getTotal() < 1) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(addressResult.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        final ServiceMultiResult<SubwayDTO> subways = addressService
                .findAllSubwayByCity(cityEnName);
        if (subways.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(subways.getResult());
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        final ServiceMultiResult<SubwayStationDTO> stationDTOS = addressService
                .findAllStationBySubway(subwayId);
        if (stationDTOS.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(stationDTOS.getResult());
    }

    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch, Model model,
                                HttpSession session, RedirectAttributes redirectAttributes) {

        // 从 rentSearch 中找，如果没有的话到 session 中找，还是没有就跳转。
        if (rentSearch.getCityEnName() == null) {
            final String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_a_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }

        final ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_a_city");
            return "redirect:/index";
        }

        model.addAttribute("currentCity", city.getResult());

        final ServiceMultiResult<SupportAddressDTO> addressResult = addressService
                .findAllRegionsByCityName(rentSearch.getCityEnName());
        if (addressResult.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_a_city");
            return "redirect:/index";
        }

        final ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);

        model.addAttribute("total", serviceMultiResult.getTotal());
        model.addAttribute("houses", serviceMultiResult.getResult());

        // 如果搜索没有指定区域名字，就全部区域都显示
        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }
        model.addAttribute("searchBody", rentSearch); // 每次查完都把信息保存到 model
        model.addAttribute("regions", addressResult.getResult());

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        // 前端js有下面两个参数保存搜索范围，防止翻页后丢失。
        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId, Model model) {
        if (houseId <= 0) {
            return "404";
        }

        final ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(houseId);
        if (!serviceResult.isSuccess()) {
            return "404";
        }

        final HouseDTO houseDTO = serviceResult.getResult();
        final Map<SupportAddress.Level, SupportAddressDTO> addressDTOMap = addressService
                .findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());

        final SupportAddressDTO city = addressDTOMap.get(SupportAddress.Level.CITY);
        final SupportAddressDTO region = addressDTOMap.get(SupportAddress.Level.REGION);

        model.addAttribute("city", city);
        model.addAttribute("region", region);

        final ServiceResult<UserDTO> userDTOServiceResult = userService.findById(houseDTO.getAdminId());

        model.addAttribute("agent", userDTOServiceResult.getResult());
        model.addAttribute("house", houseDTO);

        final ServiceResult<Long> aggResult = searchService.aggregateDistrictHouse(city.getEnName(), region.getEnName(),
                houseDTO.getDistrict());
        model.addAttribute("houseCountInDistrict", aggResult.getResult());

        return "house-detail";
    }
}
