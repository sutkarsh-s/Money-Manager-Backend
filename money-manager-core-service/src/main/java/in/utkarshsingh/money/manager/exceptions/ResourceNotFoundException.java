package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.enums.ErrorCode;

public class ResourceNotFoundException extends BaseAppException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found for id: " + id, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
