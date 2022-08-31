import { PopoverContent, Portal, usePopoverContext } from "@chakra-ui/react";
import { RemoveScroll } from "react-remove-scroll";

/**
 * ModalInputPopover
 * The body of the ModalInput component.
 * @param {React.ReactNode} children
 * @param {Object} popoverContentProps
 * @return {React.ReactNode}
 */
export const ModalInputPopover = (props) => {
  const { children, popoverContentProps, preventScroll = true } = props;
  const { isOpen } = usePopoverContext();
  return (
    <Portal>
      <RemoveScroll enabled={isOpen && preventScroll}>
        <PopoverContent
          flexGrow={1}
          flexShrink={1}
          flexBasis="fit-content"
          width="unset"
          _focus={{
            outline: "none"
          }}
          {...popoverContentProps}
        >
          {children}
        </PopoverContent>
      </RemoveScroll>
    </Portal>
  );
};

