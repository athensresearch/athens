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
export const useLayoutState = (props) => {
  const { rightSidebarWidth } = props;

  const mainContentRef = React.useRef();
  const toolbarRef = React.useRef();
  const [mainSidebarWidth, setMainSidebarWidth] = React.useState(300);
  const [unsavedRightSidebarWidth, setUnsavedRightSidebarWidth] = React.useState(rightSidebarWidth);
  const [isResizingLayout, setIsResizingLayout] = React.useState(false);
  const [isScrolledPastTitle, setIsScrolledPastTitle] = React.useState({});
  const toolbarHeight = "3rem";

  return {
    mainSidebarWidth,
    setMainSidebarWidth,
    unsavedRightSidebarWidth,
    setUnsavedRightSidebarWidth,
    isResizingLayout,
    setIsResizingLayout,
    isScrolledPastTitle,
    setIsScrolledPastTitle,
    toolbarHeight,
    mainContentRef,
    toolbarRef,
  };
};

export const LayoutProvider = (props) => {
  const { children, ...rest } = props;
  const layoutState = useLayoutState(rest);

  return <LayoutContext.Provider value={layoutState}>
    {children}
  </LayoutContext.Provider>;
}
