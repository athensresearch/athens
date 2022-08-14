import { LayoutGroupContextProps } from "framer-motion/types/context/LayoutGroupContext";
import * as React from "react";

export const LayoutContext = React.createContext(null);

export const VIEW_MODES = ["regular", "compact"];

/**
 * Transition properties for layout animation
 */
export const layoutAnimationTransition = {
  damping: 10,
  mass: 0.05,
  type: "spring"
};

/**
 * Props used to control layout changes
 */
export const layoutAnimationProps = (openWidth) => ({
  initial: { width: 0, opacity: 0 },
  animate: {
    width: openWidth,
    opacity: 1,
    transition: layoutAnimationTransition
  },
  exit: { width: 0, opacity: 0 }
});

/**
 * Instantiate state for an app layout
 */
export const useLayoutState = () => {
  const [viewMode, setViewMode] = React.useState<typeof VIEW_MODES[number]>(
    VIEW_MODES[0]
  );
  const mainContentRef = React.useRef();
  const toolbarRef = React.useRef();
  const [hasRightSidebar, setHasRightSidebar] = React.useState(true);
  const [isMainSidebarFloating, setIsMainSidebarFloating] = React.useState(
    false
  );
  const sidebarWidth = "300px";
  const toolbarHeight = "3rem";
  const rightSidebarWidth = "300px";

  return {
    sidebarWidth,
    hasRightSidebar,
    isMainSidebarFloating,
    setIsMainSidebarFloating,
    rightSidebarWidth,
    toolbarHeight,
    viewMode,
    mainContentRef,
    toolbarRef,
    setViewMode,
    setHasRightSidebar
  };
};

export const LayoutProvider = ({ children }) => {
  const context = useLayoutState();

  return <LayoutContext.Provider value={context}>{children}</LayoutContext.Provider>;
}
