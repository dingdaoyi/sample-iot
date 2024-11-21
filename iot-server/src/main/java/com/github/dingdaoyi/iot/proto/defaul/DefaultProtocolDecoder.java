package com.github.dingdaoyi.iot.proto.defaul;

import com.github.dingdaoyi.proto.inter.DeviceConnection;
import com.github.dingdaoyi.proto.model.*;
import com.github.dingdaoyi.iot.proto.defaul.model.MqttPopMessage;
import com.github.dingdaoyi.model.DTO.TslModelDTO;
import com.github.dingdaoyi.model.DTO.TslPropertyDTO;
import com.github.dingdaoyi.proto.inter.ProtocolDecoder;
import com.github.dingdaoyi.service.DeviceService;
import com.github.dingdaoyi.service.TslModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author dingyunwei
 */
@Service
@Slf4j
public class DefaultProtocolDecoder implements ProtocolDecoder {
    @Resource
    private DeviceService deviceService;
    @Resource
    private TslModelService tslModelService;

    @Override
    public String protocolKey() {
        return "system-default";
    }

    @Override
    public DecodeResult decode(DeviceRequest request) throws ProtocolException {
        if (log.isDebugEnabled()) {
            log.debug("收到解析数据:{}", new String(request.getData(), StandardCharsets.UTF_8));
        }
        Optional<TslModelDTO> optional = tslModelService.findByProductKey(request.getProtoKey());
        if (optional.isEmpty()) {
            throw new ProtocolException(request.getDeviceKey(),ExceptionType.PRODUCT_NOT_FOUND);
        }
        TslModelDTO tslModelDTO = optional.get();

        byte[] data = request.getData();
        String identifier = request.getIdentifier();
        DecodeResult decodeResult = new DecodeResult();
        switch (request.getMessageType()) {
            case PROPERTY -> {
                MqttPopMessage proMessage = JsonUtil.readValue(data, MqttPopMessage.class);
                if (proMessage != null) {
                    Optional<TslPropertyDTO> propertyDTO = tslModelDTO.propertyByIdentifier(identifier);
                    if (propertyDTO.isEmpty()) {
                        throw new ProtocolException(request.getDeviceKey(), ExceptionType.TSL_MODEL_NOT_CONFIG,proMessage.getMessageId());
                    }
                    TslPropertyDTO tslPropertyDTO = propertyDTO.get();
                    DataTypeEnum dataType = tslPropertyDTO.getDataType();
                    if (proMessage.getValue() == null) {
                        throw new ProtocolException(request.getDeviceKey(), ExceptionType.NULL_PARAM,proMessage.getMessageId());
                    }
                    try {
                        decodeResult.getDataList()
                                .add(new DeviceData(identifier, dataType, dataType.parse(proMessage.getValue())));
                    } catch (IllegalArgumentException e) {
                        throw new ProtocolException(request.getDeviceKey(), ExceptionType.INVALID_PARAM,proMessage.getMessageId());
                    }
                }
            }
        }
        return decodeResult;
    }

    @Override
    public void responseError(DeviceConnection connection, ProtocolException e) {

        connection.sendMessage();

    }
}
