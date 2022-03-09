(ns athens.views.comments.core
  (:require [athens.views.comments.inline :as inline]
            [athens.views.comments.right-side :as right-side]))


;; :author and :time in the future
(def mock-data
  [{:string "[[Brandon Toner]] Agree with the jumpiness"}
   {:string "[[Matt Vogel]] Also experiencing this. Someone closed the parent of the block I was on (I was not zoomed in) and I got kicked out of the block"}])



(def mock-data-with-author-and-time
  [{:string "Agree with the jumpiness"
    :author "Brandon Toner"
    :time "12:30pm"}
   {:string "Also experiencing this. Someone closed the parent of the block I was on (I was not zoomed in) and I got kicked out of the block"
    :author "Matt Vogel"
    :time "12:35pm"}])

(def sync-meet-interaction-1
  [{:string "I felt this too"
    :author "Sid"
    :time   "12:00"}
   {:string "same"
    :author "Jeff"
    :time   "12:01"}])


(def sync-meet-interaction-2
  [{:string "Can you elaborate on what you mean here?"
    :author "Jeff"
    :time   "12:05"}
   {:string "It was good socializing"
    :author "Alex"
    :time   "12:05"}
   {:string "Anything else apart from socializing?"
    :author "Jeff"
    :time   "12:06"}
   {:string "We also discussed about sharpening axe by doing some design problems form a book on fridays."
    :author "Alex"
    :time   "12:07"}
   {:string "Personally I was having some trouble getting coding/thinking work done after standup time, but Alex helped me with a process which I am now using and its very helpful."
    :author "Sid"
    :time   "12:20"}])


(def sync-meet-interaction-3
  [{:string "Writethrough and deep links"
    :author "Alex"
    :time   "12:09"}
   {:string "problem solved gif: https://giphy.com/gifs/jefferiesshow-funny-jim-jefferies-show-JozPUJqrzDjZRXCTI5"
    :author "Alex"
    :time   "12:09"}
   {:string "lol nice gif, any ideas how we can simulate writethrough?"
    :author "Sid"
    :time   "12:09"}
   {:string "of course, we can try using inline references"
    :author "Alex"
    :time   "12:13"}
   {:string "How?"
    :author "Sid"
    :time   "12:13"}
   {:string "let me show you"
    :author "Alex"
    :time   "12:13"}
   {:string "say if there was a way to show children of a ref, we could use inline refs to see the children for e.g in To Do page"
    :author "Alex"
    :time   "12:14"}
   {:string " we can ref the To Do of the main engineering board from the project pages\nthe result will be that we can expand inline refs on the main eng board and see stuff that is To Do in project pages"
    :author "Alex"
    :time   "12:15"}
   {:string "mindblow, this is cool af"
    :author "Sid"
    :time   "12:15"}
   {:string "wink"
    :author "Alex"
    :time   "12:16"}
   {:string "So you'd move stuff around in the project page, it shows up in the main tracking board\nnot full writethrough, but it's enough to explore a bit further in a serious usage setting"
    :author "Alex"
    :time   "12:16"}
   {:string "Sid could you take a break from talking about writethrough to talk about your original point?"
    :author "Jeff"
    :time   "12:19"}
   {:string "Oh yeah"
    :author "Sid"
    :time   "12:19"}
   {:string "Oh fuck, alex this is very cool, this can be used to explore the idea more seriously. "
    :author "Filipe"
    :time   "12:23"}
   {:string "GM Gif"
    :author "Alex"
    :time   "12:23"}
   {:string "I put a few screenshots in discord is the link: "
    :author "Filipe"
    :time   "12:25"}
   {:string "Noice"
    :author "Alex"
    :time   "12:25"}
   {:string "I raised a pr that would allow us to show children of ref link"
    :author "Filipe"
    :time   "12:30"}
   {:string "Reviewed, Merged"
    :author "Alex"
    :time   "12:32"}])

(def sync-meet-interaction-4
  [{:string "Weird for me this was good"
    :author "Filipe"
    :time   "12:10"}
   {:string "Filipe, can you elaborate on how this was good for you? I thought it was bad"
    :author "Jeff"
    :time   "12:11"}
   {:string "Oh yeah, this reminds me of our conversation about [[Product Metrics]] and the task Implemented reporting for `:right-sidebar`"
    :author "Alex"
    :time   "12:26"}])