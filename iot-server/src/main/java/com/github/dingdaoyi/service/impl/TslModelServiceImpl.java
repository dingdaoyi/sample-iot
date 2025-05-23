package com.github.dingdaoyi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.dingdaoyi.entity.ModelProperty;
import com.github.dingdaoyi.entity.Product;
import com.github.dingdaoyi.proto.model.ParamType;
import com.github.dingdaoyi.entity.enu.ServiceTypeEnum;
import com.github.dingdaoyi.proto.model.tsl.TslEvent;
import com.github.dingdaoyi.proto.model.tsl.TslModel;
import com.github.dingdaoyi.proto.model.tsl.TslProperty;
import com.github.dingdaoyi.model.vo.ModelServiceVO;
import com.github.dingdaoyi.service.*;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author dingyunwei
 */
@Service
@AllArgsConstructor
public class TslModelServiceImpl implements TslModelService {
    private final ProductService productService;
    private final ModelServiceService modelServiceService;
    private final ModelPropertyService modelPropertyService;
    private final IconService iconService;

    @Cacheable(value = "tslModelCache", key = "#productKey")
    @Override
    public Optional<TslModel> findByProductKey(String productKey) {
        return productService.getByProductKey(productKey)
                .map(product -> {
                    TslModel tslModelDTO = new TslModel();
                    tslModelDTO.setProductKey(product.getProductKey());
                    processProductData(product, tslModelDTO);
                    return tslModelDTO;
                });
    }

    private void processProductData(Product product, TslModel tslModelDTO) {
        List<ModelServiceVO> serviceList = modelServiceService.listAllByProduct(product.getId(), product.getProductTypeId());
        Map<ServiceTypeEnum, List<ModelServiceVO>> servicesMap = serviceList.stream()
                .collect(Collectors.groupingBy(ModelServiceVO::getServiceType));

        Map<Integer, TslProperty> propertiesMap = new HashMap<>();
        Map<Integer, TslProperty> paramsMap = new HashMap<>();

        modelPropertyService.listByProduct(product.getId(), product.getProductTypeId())
                .forEach(property -> {
                    if (property.getParamType() == ParamType.PROPERTY) {
                        addPropToMap(propertiesMap, property);
                    } else {
                        addPropToMap(paramsMap, property);
                    }
                });

        Map<Integer, TslProperty> allProperties = new HashMap<>(propertiesMap);
        allProperties.putAll(paramsMap);

        tslModelDTO.setProperties(new ArrayList<>(propertiesMap.values()));
        tslModelDTO.setServices(mapTslServices(servicesMap, ServiceTypeEnum.SERVICE, allProperties,
                (svc, params) -> svc.toTsl(getTslProperty(allProperties, svc.getInputParamIds()), params)));
        tslModelDTO.setEvents(mapTslServices(servicesMap, ServiceTypeEnum.EVENT, allProperties, (modelServiceVO, tslPropertyDTOS) -> {
            TslEvent tslEventDTO = new TslEvent(tslPropertyDTOS);
            BeanUtil.copyProperties(modelServiceVO, tslEventDTO);
            return tslEventDTO;
        }));
    }

    private static List<TslProperty> getTslProperty(Map<Integer, TslProperty> paramsMap, List<Integer> outputParamIds) {
        return paramsMap.entrySet()
                .stream()
                .filter(entry -> outputParamIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    private <T> List<T> mapTslServices(Map<ServiceTypeEnum, List<ModelServiceVO>> servicesMap,
                                       ServiceTypeEnum type,
                                       Map<Integer, TslProperty> allProperties,
                                       BiFunction<ModelServiceVO, List<TslProperty>, T> mapper) {
        return servicesMap.getOrDefault(type, List.of())
                .stream()
                .map(svc -> {
                    List<TslProperty> params = getTslProperty(allProperties, svc.getOutputParamIds());
                    return mapper.apply(svc, params);
                })
                .toList();
    }

    private void addPropToMap(Map<Integer, TslProperty> paramsMap, ModelProperty property) {
        if (paramsMap.containsKey(property.getParentId())) {
            TslProperty tslProperty = paramsMap.get(property.getParentId());
            List<TslProperty> children = Optional.ofNullable(tslProperty.getChildren())
                    .orElseGet(ArrayList::new);
            children.add(property.toTsl(iconService.path(property.getIconId())));
            tslProperty.setChildren(children);
        } else {
            paramsMap.put(property.getId(), property.toTsl(iconService.path(property.getIconId())));
        }
    }
}
