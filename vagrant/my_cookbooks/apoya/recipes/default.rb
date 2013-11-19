#
# Cookbook Name:: apoya
# Recipe:: default
#
# Copyright 2013, Eduardo Diaz
#
# All rights reserved - Do Not Redistribute
#

include_recipe "supervisor::default"

execute "load database" do
  cwd "/vagrant_data/apoya"
  command "lein ragtime migrate"
  timeout 7200
  action :run
  user "vagrant"
end

supervisor_service "lein-apoya" do
  command "lein with-profile production trampoline run"
  directory "/vagrant_data/apoya"
  stopsignal :INT
  action :enable
  autostart true
  user "vagrant"
end

supervisor_service "lein-cljsbuild" do
  command "lein trampoline cljsbuild auto"
  directory "/vagrant_data/apoya"
  stopsignal :INT
  action :disable
  autostart true
  user "vagrant"
end
