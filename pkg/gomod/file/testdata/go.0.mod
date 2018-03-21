module "my/thing"
require "other/thing" v1.0.2
require "new/thing" v2.3.4
exclude "old/thing" v1.2.3
replace "bad/thing" v1.4.5 => "good/thing" v1.4.5
