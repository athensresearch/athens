
import React from 'react';
import { Box, Popover, PopoverContent, PopoverTrigger, Portal, Button, ButtonGroup, Menu, MenuButton, MenuItem, MenuList } from '@chakra-ui/react';
import { EllipsisHorizontalIcon } from "@/Icons/Icons";
import {
  attrIf,
  updatePreview,
  updateActions,
  updateBlockHover,
  getIsTargetAutolink,
  getIsTargetUrlLink,
  getIsTargetPageLink,
  getIsTargetHashtag
} from './utils/utils';


export const Preview = ({ isOpen, pos, children }) => {
  if (!isOpen) return null;

  return <Popover
    isOpen={true}
    placement="bottom-start"
    autoFocus={false}
  >
    <PopoverTrigger>
      <Box
        position="fixed"
        pointerEvents="none"
        left={pos?.x}
        top={pos?.y}
      />
    </PopoverTrigger>
    <Portal>
      <PopoverContent
        pointerEvents="none"
        p={4}
        background="background.upper"
        width="20rem"
        overflow="hidden"
      >
        {children}
      </PopoverContent>
    </Portal>
  </Popover>
}

export const Actions = ({ actions, pos, isUsingActions, setIsUsingActions, hideActions }) => {

  // Exit if there are no actions to show
  if (!actions) return null;

  // Update the provided actions with
  // behavior specific to the floating actions menu
  const actionsWithBehavior = actions.map(a => ({
    ...a,
  }));

  // Divide up the actions into normal and extras (for the menu)
  const defaultActions = actionsWithBehavior.filter(a => !a.isExtra);
  const extraActions = actionsWithBehavior.filter(a => a.isExtra);

  const [isFocused, setIsFocused] = React.useState(false);
  const [isHovered, setIsHovered] = React.useState(false);

  // Handle when you're interacting with the actions
  React.useLayoutEffect(() => {
    if (isHovered || isFocused) {
      console.log('setIsUsingActions', true);
      setIsUsingActions(true);
    } else {
      console.log('setIsUsingActions', false);
      setIsUsingActions(false);
    }
  }, [isFocused, isHovered, isUsingActions]);

  // Close the actions on esc
  const handleKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
    if (e.key === 'Escape') {
      setIsFocused(false);
      hideActions();
    }
  }

  return (
    <ButtonGroup
      onFocus={() => setIsFocused(true)}
      onBlur={() => setIsFocused(false)}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onKeyDown={handleKeyDown}
      className="block-actions"
      size="xs"
      zIndex="tooltip"
      isAttached={true}
      position="absolute"
      top={pos?.y}
      left={pos?.x}
      transform="translateY(-50%) translateX(-100%)"
    >
      {defaultActions.map(a => <Button
        key={a.children}
        {...a}
      />)}
      {extraActions.length > 0 && (
        <Menu
          isLazy={true}
        >
          <MenuButton
            as={Button}
            size="xs"
            zIndex="tooltip"
          >
            <EllipsisHorizontalIcon />
          </MenuButton>
          <MenuList>
            {extraActions.map(a => <MenuItem key={a.children} {...a} />)}
          </MenuList>
        </Menu>
      )}
    </ButtonGroup>
  )
}

export const InteractionManager = ({
  children,
  shouldSetBlockIsHovered = true,
  shouldShowActions = true,
  shouldShowPreviews,
  previewEl,
  setPreview,
  onNavigateUid,
  onNavigatePage,
}) => {
  const [target, setTarget] = React.useState(null);
  const [lastE, setLastE] = React.useState(null);
  const [isScrolling, setIsScrolling] = React.useState(false);
  const [previewPos, setPreviewPos] = React.useState(null);
  const [isUsingActions, setIsUsingActions] = React.useState(false);
  const [actionsPos, setActionsPos] = React.useState(null);
  const [actions, setActions] = React.useState(null);

  // 
  const hideActions = () => {
    setActions(null);
    setActionsPos(null);
    setIsUsingActions(false);
  }

  // When hovering a page, link, breadcrumb, etc.
  const handleMouseMove = React.useCallback((e) => {

    // if the hovered event looks the same as the last event, return
    if (e.target && (e?.target === lastE?.target)) {
      console.log('skipped because same target');
      return
    };

    shouldSetBlockIsHovered && updateBlockHover(e);
    // if (isUsingActions) {
    // console.log('skipped updated actions because is using actions');
    // } else {
    shouldShowActions && updateActions(e, setActionsPos, setActions, isUsingActions);
    // }
    shouldShowPreviews && updatePreview(e, setPreviewPos, setPreview);

    setLastE(e);
  }, [target, shouldShowPreviews, shouldSetBlockIsHovered, shouldShowActions]);


  // When clicking a page, link, breadcrumb, etc.
  const handleClick = (e) => {

    const clickedPage = attrIf(e, getIsTargetPageLink, 'data-page-title');
    if (clickedPage) {
      onNavigatePage(clickedPage, e);
      return;
    }

    const clickedHashtag = attrIf(e, getIsTargetHashtag, 'data-page-title');
    if (clickedHashtag) {
      onNavigatePage(clickedHashtag, e);
      return;
    }

    // Anchor links
    const clickedUrlLink = attrIf(e, getIsTargetUrlLink, 'href');
    if (clickedUrlLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedUrlLink);
      return;
    }

    const clickedAutoLink = attrIf(e, getIsTargetAutolink, 'href');
    if (clickedAutoLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedAutoLink);
      return;
    }
  }

  return (
    <Box
      display="contents"
      onClick={handleClick}
      onMouseMove={(e) => {
        // update the target in state so we
        // can use it to memoize the mousemove event
        if (e.target !== target) { setTarget(e.target) };
        // reset scroll state in case you're
        // moving fast and you don't want to
        // wait for it to reset
        if (isScrolling) setIsScrolling(false);
        // then handle the move events
        handleMouseMove(e);
      }}
      // Create a scrolling state so we can
      // hide the actions and preview while
      // scrolling. It would be nice to avoid this,
      // but the positioning of the overlays
      // is static, so they'd be floating in space
      // without this.
      onWheel={() => {
        if (!isScrolling) {
          setIsScrolling(true)
          setTimeout(() => {
            setIsScrolling(false)
          }, 1500)
        }
      }}
    >
      {children}
      <Preview
        isOpen={!!previewEl && !isScrolling}
        pos={previewPos}
      >
        {previewEl}
      </Preview>
      <Actions
        actions={actions}
        pos={actionsPos}
        hideActions={hideActions}
        isUsingActions={isUsingActions}
        setIsUsingActions={setIsUsingActions}
      />
    </Box>)
}

