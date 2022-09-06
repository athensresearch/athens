import { PopoverAnchor } from "@chakra-ui/react";
import React from "react";

/**
 * ModalInputAnchor
 * The trigger for the ModalInput component.
 * @param {React.ReactChild}
 * @returns {React.ReactNode}
 */
export const ModalInputAnchor = ({ children }) => <PopoverAnchor>{children}</PopoverAnchor>;
